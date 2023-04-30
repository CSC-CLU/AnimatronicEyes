package org.csc_clu;

import net.java.games.input.*;

public class ControllerControlledEyes {
    public static final int eyelidMax = 800;
    public static final int eyelidCenter = 500;
    public static final int eyelidMinimum = 0;

    public static void main(String[] args) {
        // Turn off logging from jinput
        System.setProperty("jinput.loglevel", "OFF");

        // Create animatronic eyes driver
        AnimatronicEyesDriver eyes = new AnimatronicEyesDriver();
        // Create gamepad driver
        Gamepad gamepad = new Gamepad();

        try {
            while (true) {
                Thread.sleep(20);

                // Get inputs from gamepad
                ControllerInputs inputs = gamepad.getInputs();

                // Calculate eye positioning from inputs
                int eyeX = 512 - (int) (512.0 * inputs.leftX);
                int eyeY = 512 - (int) (512.0 * inputs.leftY);
                int eyelid = (int) (eyelidCenter + ((eyelidMax - eyelidCenter) * ((inputs.r2 + 1.0) / 2.0)) + ((eyelidCenter - eyelidMinimum) * ((inputs.l2 + 1.0) / -2.0)));
                boolean blink = inputs.face1;
                boolean blinkAnimation = inputs.face2;
                boolean eyeRollAnimation = inputs.face4;

                // If the mode button was pressed, exit the loop
                if (inputs.mode) {
                    break;
                }

                // Set eye positioning
                eyes.setEyePosition(eyeX, eyeY);
                eyes.setEyelidPosition(eyelid);
                if (blink) {
                    eyes.blinkEyes((byte) 10);
                } else if (blinkAnimation) {
                    eyes.blinkAnimation(100);
                } else if (eyeRollAnimation) {
//                    eyes.eyeRollAnimation(?, ?); // Not implemented yet
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eyes.close();
        }
    }
}

// Gamepad driver
class Gamepad {
    protected Controller gamepad;
    GamepadType gamepadType;

    // Names of gamepads that are not automatically identified as Controller.Type.GAMEPAD
    protected static final String[] supportedGamepads = new String[]{};

    // Gamepad types used to determine controller input mapping in getInputs()
    enum GamepadType {
        XBOX_ONE,
        UNKNOWN
    }
    public Gamepad() {
        // Get all input devices on the system
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        // Search for the first controller in the device list (should there be a gamepad selection if multiple gamepads are detected?)
        for (Controller controller : controllers) {
            if (isGamepad(controller)) {
                gamepad = controller;
                break;
            }
        }
        if (gamepad == null) {
            throw new RuntimeException("No gamepads were detected.");
        }

        String gamepadName = gamepad.getName();
//        System.out.println(gamepadName);
        if (gamepadName.contains("Microsoft X-Box One pad") ||
                gamepadName.contains("Microsoft X-Box One S pad") ||
                gamepadName.contains("Xbox Wireless Controller")) {
            gamepadType = GamepadType.XBOX_ONE;
        } else {
            gamepadType = GamepadType.UNKNOWN;
        }
    }

    protected boolean isGamepad(Controller controller) {
        if (controller.getType() == Controller.Type.GAMEPAD) {
            return true;
        } else {
            String controllerName = controller.getName();
            for (String name : supportedGamepads) {
                if (controllerName.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Get inputs from controller and map them to a standard format
    public ControllerInputs getInputs() {
        gamepad.poll();
        Component[] components = gamepad.getComponents(); // Components are the different inputs on the controller
        ControllerInputs inputs = new ControllerInputs(gamepadType);
        for(Component component : components) {
            if (gamepadType == GamepadType.XBOX_ONE) {
                switch (component.getName()) {
                    case "A" -> inputs.face1 = component.getPollData() == 1.0f;
                    case "B" -> inputs.face2 = component.getPollData() == 1.0f;
                    case "X" -> inputs.face3 = component.getPollData() == 1.0f;
                    case "Y" -> inputs.face4 = component.getPollData() == 1.0f;
                    case "Select" -> inputs.select = component.getPollData() == 1.0f;
                    case "Start" -> inputs.start = component.getPollData() == 1.0f;
                    case "Mode" -> inputs.mode = component.getPollData() == 1.0f;
                    case "x" -> inputs.leftX = component.getPollData();
                    case "y" -> inputs.leftY = component.getPollData();
                    case "rx" -> inputs.rightX = component.getPollData();
                    case "ry" -> inputs.rightY = component.getPollData();
                    case "Left Thumb" -> inputs.l1 = component.getPollData() == 1.0f;
                    case "Right Thumb" -> inputs.r1 = component.getPollData() == 1.0f;
                    case "z" -> inputs.l2 = component.getPollData();
                    case "rz" -> inputs.r2 = component.getPollData();
                    case "Left Thumb 3" -> inputs.l3 = component.getPollData() == 1.0f;
                    case "Right Thumb 3" -> inputs.r3 = component.getPollData() == 1.0f;
                    case "pov" -> {
                        float pov_hat = component.getPollData();
                        switch ((int) (pov_hat * 1000.0)) {
                            case 125 -> {
                                inputs.pov_hat_up = true;
                                inputs.pov_hat_left = true;
                            }
                            case 250 -> inputs.pov_hat_up = true;
                            case 375 -> {
                                inputs.pov_hat_up = true;
                                inputs.pov_hat_right = true;
                            }
                            case 500 -> inputs.pov_hat_right = true;
                            case 625 -> {
                                inputs.pov_hat_right = true;
                                inputs.pov_hat_down = true;
                            }
                            case 750 -> inputs.pov_hat_down = true;
                            case 875 -> {
                                inputs.pov_hat_down = true;
                                inputs.pov_hat_left = true;
                            }
                            case 1000 -> inputs.pov_hat_left = true;
                        }
                    }
                    default -> {
                        System.out.println("Unknown input: " + component.getName() + ": " + component.getPollData() * 1000);
                    }
                }
            }
        }
        return inputs;
    }
}

// Class to represent a gaepad's state
class ControllerInputs {
    protected Gamepad.GamepadType type;
    public boolean face1;
    public boolean face2;
    public boolean face3;
    public boolean face4;
    public float leftX;
    public float leftY;
    public float rightX;
    public float rightY;
    public boolean r1;
    public float r2;
    public boolean r3;
    public boolean l1;
    public float l2;
    public boolean l3;
    public boolean select;
    public boolean start;
    public boolean mode;
    public boolean pov_hat_up;
    public boolean pov_hat_down;
    public boolean pov_hat_left;
    public boolean pov_hat_right;
    public float rx;
    public float ry;
    public float rz;

    public ControllerInputs(Gamepad.GamepadType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        String format1 = "%s: %5b %s: %5b %s: %5b %s: %5b %s: %5b %s: %5b %s: %5b %s: %5.2f %5.2f %s: %5.2f %5.2f %s: %5b %s: %5.2f %s: %5b %s: %5b %s: %5.2f %s: %5b %s: %5b %s: %5b %s: %5b %s: %5b %s: %5.2f %s: %5.2f %s: %5.2f";
        String format2 = "%s: %5b %s: %5b %s: %5b %s: %5b %s: %5b %s: %5b %s: %5b %s: %5.2f %s: %5.2f %s: %5.2f %s: %5.2f %s: %5b %s: %5.2f %s: %5b %s: %5b %s: %5.2f %s: %5b %s: %5b %s: %5b %s: %5b %s: %5b %s: %5.2f %s: %5.2f %s: %5.2f";

        // Label buttons based on controller type
        if (this.type == Gamepad.GamepadType.XBOX_ONE) {
            return format1.formatted("A", face1, "B", face2, "X", face3, "Y", face4, "Start", start,
                    "Select", select, "Xbox", mode, "L Stick", leftX, leftY, "R Stick", rightX, rightY,
                    "L1", l1, "L2", l2, "L3", l3, "R1", r1, "R2", r2, "R3", r3, "Up", pov_hat_up, "Down",
                    pov_hat_down, "Left", pov_hat_left, "Right", pov_hat_right, "RX", rx, "RY", ry, "RZ", rz);
        } else {
            return format2.formatted("face1", face1, "face2", face2, "face3", face3, "face4", face4, "start",
                    start, "select", select, "mode", mode, "left X", leftX, "left Y", leftY, "right X", rightX,
                    "right Y", rightY, "L1", l1, "L2", l2, "L3", l3, "R1", r1, "R2", r2, "R3", r3, "up",
                    pov_hat_up, "down", pov_hat_down, "left", pov_hat_left, "right", pov_hat_right,
                    "RX", rx, "RY", ry, "RZ", rz);
        }
    }
}
