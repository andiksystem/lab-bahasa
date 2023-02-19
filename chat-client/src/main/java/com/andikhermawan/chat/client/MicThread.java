package com.andikhermawan.chat.client;

import com.andikhermawan.chat.commons.Message;
import com.andikhermawan.chat.commons.SoundPacket;
import com.andikhermawan.chat.commons.Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class MicThread extends Thread {

    private boolean started;
    public static double amplification = 1.0;
    private final ObjectOutputStream toServer;
    private final TargetDataLine mic;

    public MicThread(ObjectOutputStream toServer) throws LineUnavailableException {
        started = true;
        this.toServer = toServer;
        AudioFormat af = SoundPacket.defaultFormat;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);
        mic = (TargetDataLine) (AudioSystem.getLine(info));
        mic.open(af);
        mic.start();
    }

    @Override
    public void run() {
        while (started) {
            if (mic.available() >= SoundPacket.defaultDataLenght) {
                byte[] buff = new byte[SoundPacket.defaultDataLenght];
                while (mic.available() >= SoundPacket.defaultDataLenght) {
                    mic.read(buff, 0, buff.length);
                }

                try {

                    long tot = 0;
                    for (int i = 0; i < buff.length; i++) {
                        buff[i] *= amplification;
                        tot += Math.abs(buff[i]);
                    }
                    tot *= 2.5;
                    tot /= buff.length;

                    Message m;
                    if (tot == 0) {
                        m = new Message(-1, -1, new SoundPacket(null));
                    } else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        GZIPOutputStream go = new GZIPOutputStream(baos);
                        go.write(buff);
                        go.flush();
                        go.close();
                        baos.flush();
                        baos.close();
                        m = new Message(-1, -1, new SoundPacket(baos.toByteArray()));  
                    }
                    toServer.writeObject(m);
                } catch (IOException ex) {
                    started = false;
                }
            } else {
                Utils.sleep(10); 
            }
        }
    }
}
