package org.csc_clu;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Arrays;

public class AnimatronicEyesDriver {
    public static void main(String[] args) {
        System.out.println("Connecting to animatronic eyes");
        AnimatronicEyesDriver eyes = new AnimatronicEyesDriver();
        System.out.println("Connected.");

        try {
            int delay = 500; // Delay in ms

            // Reinitialize eyes
            eyes.initialize();
            Thread.sleep(delay);

            // Test eye movement
            eyes.setEyePosition(0, 512);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 512);
            Thread.sleep(delay);
            eyes.setEyePosition(1023, 512);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 512);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 1023);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 512);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 0);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 512);
            Thread.sleep(delay);

            eyes.setEyePosition(0, 0);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 512);
            Thread.sleep(delay);
            eyes.setEyePosition(1023, 0);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 512);
            Thread.sleep(delay);
            eyes.setEyePosition(0, 1023);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 512);
            Thread.sleep(delay);
            eyes.setEyePosition(1023, 1023);
            Thread.sleep(delay);
            eyes.setEyePosition(512, 512);
            Thread.sleep(delay);

            // Test eyelid movement
            eyes.setEyelidPosition(128);
            Thread.sleep(delay);
            eyes.setEyelidPosition(0);
            Thread.sleep(delay);
            eyes.setEyelidPosition(128);
            Thread.sleep(delay);
            eyes.setEyelidPosition(512);
            Thread.sleep(delay);
            eyes.setEyelidPosition(640);
            Thread.sleep(delay);
            eyes.setEyelidPosition(750);
            Thread.sleep(delay);
            eyes.setEyelidPosition(640);
            Thread.sleep(delay);
            eyes.setEyelidPosition(512);
            Thread.sleep(delay);

            // Test blink
            eyes.blinkEyes((byte) 10);
            Thread.sleep(delay);
            eyes.blinkEyes((byte) 20);
            Thread.sleep(delay);

            // Test animations
            eyes.blinkAnimation((byte) 100);
            Thread.sleep(delay);
//            eyes.blinkAnimation((byte) 200);
            Thread.sleep(delay);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eyes.close();
        }
    }

    public static final int driverMajorVersion = 0;
    public static final int driverMinorVersion = 1;
    public static final int driverPatchVersion = 1;
    public static int arduinoMajorVersion;
    public static int arduinoMinorVersion;
    public static int arduinoPatchVersion;
    private static SerialPort comm;

    private static final int DEFAULT_TIMEOUT = 25; //TIMEOUT = DEFAULT_TIMEOUT * 20ms

    private static final int[][] supportedArduinoVersions = new int[][]{
            {0,5,-1}, // 0.5.X
            {1,0,-1}}; //1.0.X

    private enum COMMANDS {
        // PC to Arduino
        INITIALIZE_EYES((byte)(0x69), "Initialize eyes"),
        EYE_VERSION((byte)(0x56), "Eye version info"),
        EYE_POSITION((byte)(0x70), "Eye position"),
        EYELID_POSITION((byte)(0x6C), "Eyelid position"),
        BLINK((byte)(0x62), "Blink"),
        ANIMATION((byte)(0x61), "Animation"),
        // Arduino to PC
        READY((byte)(0x52), "Ready"),
        ACK((byte)(0x06), "Acknowledge"),
        NAK((byte)(0x15), "Negative Acknowledge");

        public final byte value;
        public final String name;

        COMMANDS(byte value, String name) {
            this.value = value;
            this.name = name;
        }
    }

    public AnimatronicEyesDriver() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        if (serialPorts.length == 0) {
            throw new RuntimeException("No serial devices were found.");
        } else if (serialPorts.length == 1) {
            comm = serialPorts[0];
        } else {
            comm = serialPorts[0];
        }
//        System.out.println("Comm device: " + comm.getPortDescription());

//        System.out.println("Opening comm device");
        comm.openPort();
        comm.flushIOBuffers();

//        System.out.println("Waiting for device to be ready");
        waitForReady();

//        System.out.println("Initializing animatronics");
        initialize();

//        System.out.println("Getting version information");
        getEyeVersionInfo();
//        System.out.println("Checking version information");
        // Check version compatibility here
        for (int[] supportedArduinoVersion : supportedArduinoVersions) {
            if (arduinoMajorVersion == supportedArduinoVersion[0] || supportedArduinoVersion[0] == -1) {
                if (arduinoMinorVersion == supportedArduinoVersion[1] || supportedArduinoVersion[1] == -1) {
                    if (arduinoMajorVersion == supportedArduinoVersion[2] || supportedArduinoVersion[2] == -1) {
//                        System.out.println("Version information matches");
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("Driver does not support arduino version.");
    }

    private boolean waitForReady() {
        return waitForReady(DEFAULT_TIMEOUT);
    }
    private boolean waitForReady(int timeout) {
        for (int i = 0; i < timeout; i++) {
            try {
                while (comm.bytesAvailable() == 0)
                    Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (comm.bytesAvailable() > 0) {
                byte[] readBuffer = new byte[1];
                comm.readBytes(readBuffer, readBuffer.length);
                if (readBuffer[0] == COMMANDS.READY.value) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean waitForAcknowledge() {
        return waitForAcknowledge(DEFAULT_TIMEOUT);
    }
    private boolean waitForAcknowledge(int timeout) {
        for (int i = 0; i < timeout; i++) {
            try {
                while (comm.bytesAvailable() == 0)
                    Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (comm.bytesAvailable() > 0) {
                byte[] readBuffer = new byte[1];
                comm.readBytes(readBuffer, readBuffer.length);
                if (readBuffer[0] == COMMANDS.ACK.value) {
                    return true;
                } else if (readBuffer[0] == COMMANDS.NAK.value) {
                    return false;
                }
            }
        }
        return false;
    }

    public void initialize() {
        byte[] command = new byte[]{COMMANDS.INITIALIZE_EYES.value, 0};
        command[command.length-1] = calculateChecksum(command);
        comm.writeBytes(command, command.length);
        comm.writeBytes(new byte[]{0,0}, 2); // To make sure command is sent
        waitForAcknowledge();
    }

    private void getEyeVersionInfo() {
        byte[] command = new byte[]{COMMANDS.EYE_VERSION.value, 0};
        command[command.length-1] = calculateChecksum(command);
        comm.writeBytes(command, command.length);
        comm.writeBytes(new byte[]{0,0}, 2); // To make sure command is sent
        waitForAcknowledge();
        while (comm.bytesAvailable() < 3) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        byte[] bytes = new byte[4];
        comm.readBytes(bytes, bytes.length);
        arduinoMajorVersion = bytes[0];
        arduinoMinorVersion = bytes[1];
        arduinoPatchVersion = bytes[2];
        int arduinoVersionDateLength = bytes[3];
        while (comm.bytesAvailable() < arduinoVersionDateLength - 1) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        bytes = new byte[arduinoVersionDateLength];
        comm.readBytes(bytes, bytes.length);
        StringBuilder arduinoVersionDate = new StringBuilder();
        for (byte aByte : bytes) {
            arduinoVersionDate.append((char)aByte);
        }
        System.out.printf("Animatronic eyes v%d.%d.%d%n", arduinoMajorVersion, arduinoMinorVersion, arduinoPatchVersion);
        System.out.println("Compiled on " + arduinoVersionDate);
        waitForReady();
    }

    public void setEyePosition(int xval, int yval) {
        byte[] command = new byte[] {COMMANDS.EYE_POSITION.value,
                (byte) ((xval >> 8) & 0xFF), (byte) (xval & 0xFF),
                (byte) ((yval >> 8) & 0xFF), (byte) (yval & 0xFF), 0};
        command[command.length-1] = calculateChecksum(command);
        comm.writeBytes(command, command.length);
        waitForAcknowledge();

    }

    public void setEyelidPosition(int trimval) {
        byte[] command = new byte[] {COMMANDS.EYELID_POSITION.value,
                (byte) ((trimval >> 8) & 0xFF), (byte) (trimval & 0xFF), 0};
        command[command.length-1] = calculateChecksum(command);
        comm.writeBytes(command, command.length);
        waitForAcknowledge();
    }

    public void blinkEyes(byte delay) {
        byte[] command = new byte[] {COMMANDS.BLINK.value, delay, 0};
        command[command.length-1] = calculateChecksum(command);
        comm.writeBytes(command, command.length);
        if (waitForAcknowledge()) {
            waitForReady();
        }
    }

    public void blinkAnimation(int delay) {
        int animationNumber = 1;
        byte numArgs = 2;
        byte[] command = new byte[] {COMMANDS.ANIMATION.value,
                (byte) ((animationNumber >> 4) & 0xFF),
                (byte) (((animationNumber & 0x0F) << 4) + numArgs),
                (byte) ((delay >> 8) & 0xFF),
                (byte) (delay & 0xFF), 0};
        command[command.length-1] = calculateChecksum(command);
//        System.out.println(Arrays.toString(command));
        comm.writeBytes(command, command.length);
        if (waitForAcknowledge()) {
            waitForReady();
        }
    }

    public void eyeRollAnimation(int intensity, int delay) {
        int animationNumber = 2;
        byte numArgs = 4;
        byte[] command = new byte[] {COMMANDS.ANIMATION.value,
                (byte) ((animationNumber >> 4) & 0xFF),
                (byte) (((animationNumber & 0x0F) << 4) + numArgs),
                (byte) ((intensity >> 8) & 0xFF),
                (byte) (intensity & 0xFF),
                (byte) ((delay >> 8) & 0xFF),
                (byte) (delay & 0xFF), 0};
        command[command.length-1] = calculateChecksum(command);
//        System.out.println(Arrays.toString(command));
        comm.writeBytes(command, command.length);
        if (waitForAcknowledge()) {
            waitForReady();
        }
    }

    private byte calculateChecksum(byte[] bytes) {
        int checksum = 0;
        for (int i = 0; i < bytes.length; i++) {
            checksum += ((int)bytes[i]) & 0xFF;
        }
        return (byte) (checksum % 256);
    }

    public void close() {
        comm.closePort();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public String toString() {
        return "Animatronic Eyes Driver v" + driverMajorVersion + "." + driverMinorVersion + "." + driverPatchVersion +
                "\nComm device: " + comm.getPortDescription();
    }
}
