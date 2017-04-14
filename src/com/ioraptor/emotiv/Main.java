package com.ioraptor.emotiv;

import com.ioraptor.emotiv.iedk.*;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * @author Ney Moura
 * @since 14/04/2017
 */
public class Main {

    public static void main(String[] args) {

        Pointer eEvent = Edk.INSTANCE.IEE_EmoEngineEventCreate();
        Pointer eState = Edk.INSTANCE.IEE_EmoStateCreate();

        IntByReference userID = null;
        int state = 0;
        boolean onStateChanged = false;
        boolean readyToCollect = false;

        userID = new IntByReference(0);

        if (emotivConnect()) return;

        while (true) {
            state = Edk.INSTANCE.IEE_EngineGetNextEvent(eEvent);

            // New event needs to be handled
            if (state == EdkErrorCode.EDK_OK.ToInt()) {

                int eventType = Edk.INSTANCE.IEE_EmoEngineEventGetType(eEvent);

                Edk.INSTANCE.IEE_EmoEngineEventGetUserId(eEvent, userID);

                switch(eventType)
                {
                    case 0x0010:
                        System.out.println("User added");
                        readyToCollect = true;
                        break;
                    case 0x0020:
                        System.out.println("User removed");
                        readyToCollect = false; 		//just single connection
                        break;
                    case 0x0040:
                        onStateChanged = true;
                        Edk.INSTANCE.IEE_EmoEngineEventGetEmoState(eEvent, eState);
                        break;
                    default:
                        break;
                }

                if (readyToCollect && onStateChanged)
                {

                    System.out.println("Blink: " + EmoState.INSTANCE.IS_FacialExpressionIsBlink(eState));
//                    System.out.println("Stress: " + PerformanceMetrics.INSTANCE.IS_PerformanceMetricGetStressScore(eState));

                    System.out.println("Focus :" + PerformanceMetrics.INSTANCE.IS_PerformanceMetricGetFocusScore(eState));

                }

            } else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
                System.out.println("Internal error in Emotiv Engine!");
                break;
            }

        }

        emotivDisconnect();

    }

    private static void emotivDisconnect() {
        Edk.INSTANCE.IEE_EngineDisconnect();
        System.out.println("Disconnected!");
    }

    private static boolean emotivConnect() {
        if (Edk.INSTANCE.IEE_EngineConnect("Emotiv Systems-5") !=
                EdkErrorCode.EDK_OK.ToInt()) {
            System.out.println("Emotiv Engine start up failed.");
            return true;
        }
        return false;
    }

}
