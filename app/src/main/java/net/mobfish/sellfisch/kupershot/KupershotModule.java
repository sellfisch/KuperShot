package net.mobfish.sellfisch.kupershot;

import android.content.Context;
import android.util.Log;

import com.path.android.jobqueue.BaseJob;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.di.DependencyInjector;
import com.path.android.jobqueue.log.CustomLogger;
import com.path.android.jobqueue.network.NetworkUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import net.mobfish.sellfisch.kupershot.core.job.ImageUploadJob;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Bajic Dusko (www.bajicdusko.com) on 21-Jul-16.
 */

@Module
public class KupershotModule {

    Context context;

    public KupershotModule(Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    public Context provideContext() {
        return context;
    }

    @Singleton
    @Provides
    public Bus provideBus() {
        return new Bus(ThreadEnforcer.ANY);
    }

    @Singleton
    @Provides
    public JobManager provideJobManager(final Context context) {
        Configuration configuration = new Configuration.Builder(context)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .networkUtil(new NetworkUtil() {
                    @Override
                    public boolean isConnected(Context context) {
                        return true;
                    }
                })
                .injector(new DependencyInjector() {
                    @Override
                    public void inject(BaseJob job) {
                        KupershotComponent component = ((KupershotApplication) context.getApplicationContext()).getComponent();
                        if (job instanceof ImageUploadJob) {
                            component.inject((ImageUploadJob) job);
                        }
                    }
                })
                .build();
        return new JobManager(context, configuration);
    }
}
