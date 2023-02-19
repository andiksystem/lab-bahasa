package com.andikhermawan.chat.client;

import com.andikhermawan.chat.commons.Message;
import com.andikhermawan.chat.commons.SoundPacket;
import com.andikhermawan.chat.commons.Utils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioChannel extends Thread {

    private boolean started;
    private final long chatId;
    private final ArrayList<Message> queue;
    private int lastSoundPacketLen = SoundPacket.defaultDataLenght;
    private long lastPacketTime = System.nanoTime();
    private SourceDataLine speaker = null;

    public AudioChannel(long chatId) {
        this.started = true;
        this.queue = new ArrayList<>();
        this.chatId = chatId;
    }

    public boolean canKill() {
        return System.nanoTime() - lastPacketTime > 5000000000L;
    }

    public void closeAndKill() {
        if (speaker != null) {
            speaker.close();
        }
        started = false;
    }

    public long getChId() {
        return chatId;
    }

    public void addToQueue(Message m) {
        queue.add(m);
    }

    @Override
    public void run() {
        try {
            AudioFormat af = SoundPacket.defaultFormat;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(af);
            speaker.start();

            while (started) {
                if (queue.isEmpty()) {
                    Utils.sleep(10);
                } else {
                    lastPacketTime = System.nanoTime();
                    Message in = queue.get(0);
                    queue.remove(in);
                    if (in.getData() instanceof SoundPacket) { 
                        SoundPacket m = (SoundPacket) (in.getData());
                        if (m.getData() == null) {
                            byte[] noise = new byte[lastSoundPacketLen];
                            for (int i = 0; i < noise.length; i++) {
                                noise[i] = (byte) ((Math.random() * 3) - 1);
                            }
                            speaker.write(noise, 0, noise.length);
                        } else {
                            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(m.getData()));
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            for (;;) {
                                int b = gis.read();
                                if (b == -1) {
                                    break;
                                } else {
                                    baos.write((byte) b);
                                }
                            }

                            byte[] toPlay = baos.toByteArray();
                            speaker.write(toPlay, 0, toPlay.length);
                            lastSoundPacketLen = m.getData().length;
                        }
                    } 
                }
            }
        } catch (IOException | LineUnavailableException e) { 
            System.out.println("receiverThread " + chatId + " error: " + e.toString());
            if (speaker != null) {
                speaker.close();
            }
            started = false;
        }
    }
}
