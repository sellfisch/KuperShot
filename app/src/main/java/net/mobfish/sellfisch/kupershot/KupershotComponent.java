package net.mobfish.sellfisch.kupershot;

import net.mobfish.sellfisch.kupershot.core.job.ImageUploadJob;
import net.mobfish.sellfisch.kupershot.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Bajic Dusko (www.bajicdusko.com) on 21-Jul-16.
 */

@Singleton
@Component(modules = {
        KupershotModule.class
})
public interface KupershotComponent {
    void inject(MainActivity activity);

    void inject(ImageUploadJob job);
}
