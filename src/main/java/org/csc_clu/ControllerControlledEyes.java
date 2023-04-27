package org.csc_clu;

import net.java.games.input.*;

public class ControllerControlledEyes {
    public static final int eyelidMax = 800;
    public static final int eyelidCenter = 500;
    public static final int eyelidMinimum = 0;

    public static void main(String[] args) {
        System.setProperty("jinput.loglevel", "OFF");
        AnimatronicEyesDriver eyes = new AnimatronicEyesDriver();
        Gamepad gamepad = new Gamepad();

        try {
            while (true) {
                Thread.sleep(20);

                ControllerInputs inputs = gamepad.getInputs();

                int eyeX = 512 - (int) (512.0 * inputs.leftX);
                int eyeY = 512 - (int) (512.0 * inputs.leftY);
                int eyelid = (int) (eyelidCenter + ((eyelidMax - eyelidCenter) * ((inputs.r2 + 1.0) / 2.0)) + ((eyelidCenter - eyelidMinimum) * ((inputs.l2 + 1.0) / -2.0)));
                boolean blink = inputs.face1;
                boolean blinkAnimation = inputs.face2;
                boolean eyeRollAnimation = inputs.face4;

                if (inputs.mode) {
                    break;
                }

                eyes.setEyePosition(eyeX, eyeY);
                eyes.setEyelidPosition(eyelid);
                if (blink) {
                    eyes.blinkEyes((byte) 10);
                } else if (blinkAnimation) {
                    eyes.blinkAnimation(100);
                } else if (eyeRollAnimation) {
//                    eyes.eyeRollAnimation(?, ?);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eyes.close();
        }
    }
}

class Gamepad {
    protected Controller gamepad;
    GamepadType gamepadType;

    protected static final String[] supportedGamepads = new String[]{};

    enum GamepadType {
        XBOX_ONE,
        UNKNOWN
    }
    public Gamepad() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for (Controller controller : controllers) {
            if (isGamepad(controller)) {
                gamepad = controller;
                break;
            }
        }
        if (gamepad == null) {
            throw new RuntimeException("No gamepads were detected.");
        }
//        System.out.println(gamepad.getName());

        String gamepadName = gamepad.getName();
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

    public ControllerInputs getInputs() {
        gamepad.poll();
        Component[] components = gamepad.getComponents();
        ControllerInputs inputs = new ControllerInputs();
        for(Component component : components) {
            if (gamepadType == GamepadType.XBOX_ONE) {
                switch (component.getName()) {
                    case "A":
                        inputs.face1 = component.getPollData()==1.0f;
                        break;
                    case "B":
                        inputs.face2 = component.getPollData()==1.0f;
                        break;
                    case "X":
                        inputs.face3 = component.getPollData()==1.0f;
                        break;
                    case "Y":
                        inputs.face4 = component.getPollData()==1.0f;
                        break;
                    case "Select":
                        inputs.select = component.getPollData()==1.0f;
                        break;
                    case "Start":
                        inputs.start = component.getPollData()==1.0f;
                        break;
                    case "Mode":
                        inputs.mode = component.getPollData()==1.0f;
                        break;
                    case "x":
                        inputs.leftX = component.getPollData();
                        break;
                    case "y":
                        inputs.leftY = component.getPollData();
                        break;
                    case "rx":
                        inputs.rightX = component.getPollData();
                        break;
                    case "ry":
                        inputs.rightY = component.getPollData();
                        break;
                    case "Left Thumb":
                        inputs.l1 = component.getPollData()==1.0f;
                        break;
                    case "Right Thumb":
                        inputs.r1 = component.getPollData()==1.0f;
                        break;
                    case "z":
                        inputs.l2 = component.getPollData();
                        break;
                    case "rz":
                        inputs.r2 = component.getPollData();
                        break;
                    case "Left Thumb 3":
                        inputs.l3 = component.getPollData()==1.0f;
                        break;
                    case "Right Thumb 3":
                        inputs.r3 = component.getPollData()==1.0f;
                        break;
                    case "pov":
                        float pov_hat = component.getPollData();
                        inputs.pov_hat = pov_hat;
                        switch ((int) (pov_hat*1000.0)) {
                            case 125:
                                inputs.pov_hat_up = true;
                                inputs.pov_hat_left = true;
                                break;
                            case 250:
                                inputs.pov_hat_up = true;
                                break;
                            case 375:
                                inputs.pov_hat_up = true;
                                inputs.pov_hat_right = true;
                                break;
                            case 500:
                                inputs.pov_hat_right = true;
                                break;
                            case 625:
                                inputs.pov_hat_right = true;
                                inputs.pov_hat_down = true;
                                break;
                            case 750:
                                inputs.pov_hat_down = true;
                                break;
                            case 875:
                                inputs.pov_hat_down = true;
                                inputs.pov_hat_left = true;
                                break;
                            case 1000:
                                inputs.pov_hat_left = true;
                                break;
                        }
                        break;
                    default:
                        System.out.println("Unknown input: " + component.getName() + ": " + component.getPollData()*1000);
//                        System.out.print(component.getName() + ": " + component.getPollData() + ", ");
                }
            }
        }
//        System.out.println();
        return inputs;
    }
}

class ControllerInputs {
    boolean face1;
    boolean face2;
    boolean face3;
    boolean face4;
    float leftX;
    float leftY;
    float rightX;
    float rightY;
    boolean r1;
    float r2;
    boolean r3;
    boolean l1;
    float l2;
    boolean l3;
    boolean select;
    boolean start;
    boolean mode;
    float pov_hat;
    boolean pov_hat_up;
    boolean pov_hat_down;
    boolean pov_hat_left;
    boolean pov_hat_right;
    float rx;
    float ry;
    float rz;

    @Override
    public String toString() {
        return "A: %5b B: %5b X: %5b Y: %5b Start: %5b Select: %5b Mode: %5b L Stick: %5.2f %5.2f R Stick: %5.2f %5.2f L1: %5b L2: %5.2f L3: %5b R1: %5b R2: %5.2f R3: %5b Up: %5b Down: %5b Left: %5b Right: %5b RX: %5.2f RY: %5.2f RZ: %5.2f"
                .formatted(face1, face2, face3, face4, start, select, mode, leftX, leftY, rightX, rightY, l1, l2, l3, r1, r2, r3, pov_hat_up, pov_hat_down, pov_hat_left, pov_hat_right, rx, ry, rz);
    }
}
