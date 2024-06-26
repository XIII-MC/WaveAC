package com.xiii.wave.checks;

import com.xiii.wave.checks.annotations.Testing;
import com.xiii.wave.checks.impl.autofish.AutoFishA;
import com.xiii.wave.checks.impl.autofish.AutoFishB;
import com.xiii.wave.checks.impl.fly.FlyA;
import com.xiii.wave.checks.impl.ground.GroundA;
import com.xiii.wave.checks.impl.speed.SpeedA;
import com.xiii.wave.checks.types.Check;
import com.xiii.wave.managers.profile.Profile;
import com.xiii.wave.processors.packet.client.ClientPlayPacket;
import com.xiii.wave.processors.packet.server.ServerPlayPacket;

import java.util.Arrays;

public class CheckHolder {

    private final Profile profile;
    private Check[] checks;
    private int checksSize;
    private boolean testing; //Used for testing new checks

    public CheckHolder(Profile profile) {
        this.profile = profile;
    }

    public void runChecks(final ClientPlayPacket clientPlayPacket) {
        /*
        Fastest way to loop through many objects, If you think this is stupid
        Then benchmark the long term perfomance yourself with many profilers and java articles.
         */
        for (int i = 0; i < this.checksSize; i++) this.checks[i].handle(clientPlayPacket);
    }

    public void runChecks(final ServerPlayPacket serverPlayPacket) {
        /*
        Fastest way to loop through many objects, If you think this is stupid
        Then benchmark the long term perfomance yourself with many profilers and java articles.
         */
        for (int i = 0; i < this.checksSize; i++) this.checks[i].handle(serverPlayPacket);
    }

    public void registerAll() {

        /*
         * Check initialization
         */
        addChecks(

                new FlyA(this.profile),
                new AutoFishA(this.profile),
                new AutoFishB(this.profile),
                new SpeedA(this.profile),
                new GroundA(this.profile)

        );

        /*
        Remove checks if a testing check is present.
         */
        testing:
        {

            /*
            Testing check not present, break.
             */
            if (!this.testing) break testing;

            /*
            Remove the rest of the checks since a testing check is present.
             */
            this.checks = Arrays.stream(this.checks)
                    .filter(check -> check.getClass().isAnnotationPresent(Testing.class))
                    .toArray(Check[]::new);

            /*
            Update the size since we're only going to be running one check.
             */
            this.checksSize = 1;
        }
    }

    private void addChecks(Check... checks) {

        /*
        Create a new check array to account for reloads.
         */
        this.checks = new Check[0];

        /*
        Reset the check size to account for reloads
         */
        this.checksSize = 0;

        /*
        Loop through the input checks
         */
        for (Check check : checks) {

            /*
            Check if this is being used by a GUI, where we put null as the profile
            Or a check with the @Testing annotation is present or disabled.
             */
            if (this.profile != null && (!check.isEnabled() || isTesting(check))) continue;

            /*
            Copy the original array and increment the size just like an ArrayList.
             */
            this.checks = Arrays.copyOf(this.checks, this.checksSize + 1);

            /*
            Update the check.
             */
            this.checks[this.checksSize] = check;

            /*
            Update the check size variable for improved looping perfomance
             */
            this.checksSize++;
        }
    }

    /**
     * If a check with the testing annotation is present, It'll set the testing boolean to true, load it and then
     * Prevent any other checks from registering.
     */
    private boolean isTesting(Check check) {

        if (this.testing) return true;

        /*
        Update the variable and return false in order to register this check
        But not the next ones.
         */
        if (check.getClass().isAnnotationPresent(Testing.class)) this.testing = true;

        return false;
    }

    public Check[] getChecks() {
        return checks;
    }
}
