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

                    read(userID);

                }

            } else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
                System.out.println("Internal error in Emotiv Engine!");
                break;
            }

        }

        emotivDisconnect();

    }

    private static void read(IntByReference userID) {

        DoubleByReference alpha     = new DoubleByReference(0);
        DoubleByReference low_beta  = new DoubleByReference(0);
        DoubleByReference high_beta = new DoubleByReference(0);
        DoubleByReference gamma     = new DoubleByReference(0);
        DoubleByReference theta     = new DoubleByReference(0);

        int successfulReads = 0;

        double alphaSum = 0.0;
        double lowBetaSum = 0.0;
        double highBetaSum = 0.0;
        double gammaSum = 0.0;
        double thetaSum = 0.0;

        for(int i = 3 ; i < 17 ; i++){

            int result = Edk.INSTANCE.IEE_GetAverageBandPowers(userID.getValue(), i, theta, alpha, low_beta, high_beta, gamma);

            if(result == EdkErrorCode.EDK_OK.ToInt()){

                alphaSum += alpha.getValue();
                lowBetaSum += low_beta.getValue();
                highBetaSum += high_beta.getValue();
                gammaSum += gamma.getValue();
                thetaSum += theta.getValue();

                successfulReads++;

            }

        }

        double alphaAvg = alphaSum/successfulReads;
        double lowBetaAvg = lowBetaSum/successfulReads;
        double highBetaAvg = highBetaSum/successfulReads;
        double gammaAvg = gammaSum/successfulReads;
        double thetaAvg = thetaSum/successfulReads;

        System.out.println(
                  zeroNaN(alphaAvg) + ","
                + zeroNaN(lowBetaAvg) + ","
                + zeroNaN(highBetaAvg) + ","
                + zeroNaN(gammaAvg) + ","
                + zeroNaN(thetaAvg)
        );

    }

    private static double zeroNaN(double d){
        if(Double.isNaN(d)){
            return 0.0;
        }else{
            return d;
        }
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
