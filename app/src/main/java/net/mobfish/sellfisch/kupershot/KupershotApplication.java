package net.mobfish.sellfisch.kupershot;

import android.app.Application;

/**
 * Created by Bajic Dusko (www.bajicdusko.com) on 21-Jul-16.
 */
public class KupershotApplication extends Application {

    KupershotComponent kupershotComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        kupershotComponent = DaggerKupershotComponent.builder().kupershotModule(new KupershotModule(this)).build();
    }

    public KupershotComponent getComponent() {
        return kupershotComponent;
    }
}
