import org.jtransforms.fft.FloatFFT_1D;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Test {

    public static void getBins(final float[] samples){
        final FloatFFT_1D fft = new FloatFFT_1D(samples.length/2);
        fft.complexForward(samples);

        for(int pos = 0; pos < samples.length; pos+=2){
            samples[pos/2] = (float)Math.abs(Math.log10(Math.sqrt((samples[pos] * samples[pos]) + (samples[pos+1] * samples[pos+1]))));
        }
    }

    public static void main(final String[] args) throws Exception {
        final JFrame frame = new JFrame("Moo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final int fftWidth = 128;

        final Panel p = new Panel(new File("/dev/shm/sig"), fftWidth/2);
        p.setSize(fftWidth/2, 255);
        frame.add(p);

        frame.pack();
        frame.setSize(fftWidth/2, 256);
        frame.setVisible(true);

        while(true){
            p.repaint();
            Thread.sleep(10);
        }
    }

    private static class Panel extends JPanel {
        private final DataInputStream in;
        private final float[] samples;
        private final FloatFFT_1D fft;
        private final byte[] floatBuffer = new byte[4];
        private final float[] avg;
        private final float alpha = 0.01f;
        private final float multiplier = 10f;

        public Panel(final File file, final int fftSize) throws IOException {
            in = new DataInputStream(new FileInputStream(file));
            samples = new float[fftSize*2];
            fft = new FloatFFT_1D(fftSize);
            avg = new float[fftSize];

            Arrays.fill(avg, 0f);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            try {
                for (int a = 0; a < samples.length; a += 2) {
                    in.read(floatBuffer);
                    samples[a] = ByteBuffer.wrap(floatBuffer).order(ByteOrder.LITTLE_ENDIAN).getFloat();
//                    samples[a+1] = 0;
                    in.read(floatBuffer);
                    samples[a+1] = ByteBuffer.wrap(floatBuffer).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                }


                g.setColor(Color.black);

                System.out.println(samples[0]+" "+samples[1]);
                fft.complexForward(samples);
//                window(samples, samples.length, 0);

//                System.out.println(samples[0]+"="+samples[1]);

                int val;

//                for(int a = samples.length/2; a >= 0; a-=2){
//                    avg[a/2] = (avg[a/2] * (1.0f - alpha)) + (float)((Math.abs(Math.log10(Math.sqrt( (samples[a] * samples[a]) + (samples[a+1] * samples[a+1]) ))* multiplier) ) * alpha);
//
//                    g.drawLine(getWidth() - (a / 2), 0, getWidth() - (a / 2), (int) avg[a / 2]);
//                }

                System.out.println(samples[0]+" "+samples[1]);
                System.out.println((float)((Math.abs(Math.log10(Math.sqrt( (samples[0] * samples[0]) + (samples[1] * samples[1]) ))* multiplier) )));

                for(int a = 0; a < samples.length; a+=2){


//                    System.out.println(samples[a+1]);
                    avg[a/2] = (avg[a/2] * (1.0f - alpha)) + (float)((Math.abs(Math.log10(Math.sqrt( (samples[a] * samples[a]) + (samples[a+1] * samples[a+1]) ))* multiplier) ) * alpha);
                    //val = (int)(Math.abs(Math.log10(Math.sqrt( (samples[a] * samples[a]) + (samples[a+1] * samples[a+1]) ))) * 50);
                    if(avg[a/2] > getHeight()){
                        g.drawLine(getWidth()-(a/2), 0, getWidth()-(a/2), 0);
                    }else {
                        g.drawLine(getWidth() - (a / 2), 0, getWidth() - (a / 2), (int) avg[a / 2]);
                    }
                }

                System.exit(1);


            }catch(final IOException e){
                return;
            }
        }
    }

    public static void window(final float[] samples, int size, int pos){

        for(int a = 0; a < size; a++){
            int j = a - pos;
            samples[a] = (float)(samples[a] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * j / size)));
        }
    }
}
