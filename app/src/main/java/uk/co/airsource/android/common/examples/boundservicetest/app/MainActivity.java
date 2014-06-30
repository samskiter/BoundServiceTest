package uk.co.airsource.android.common.examples.boundservicetest.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity implements BoundService.TestRequest.RequestResultListener,
                                                      ModelObject.LongRunningListener
{

    private RetainedFragment dataFragment;
    private BoundService.TestRequest mRequest;
    private Button mQuickButton;
    private Button mSlowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag("data");

        // create the fragment the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment, "data").commit();
        }

        mQuickButton = (Button)findViewById(R.id.quick);
        mSlowButton = (Button)findViewById(R.id.slow);

        if (savedInstanceState != null)
        {
            if (!savedInstanceState.getBoolean("quick"))
            {
                mQuickButton.setEnabled(false);
                mQuickButton.setText("Doing Quick Stuff");
            }
            if (!savedInstanceState.getBoolean("slow"))
            {
                mSlowButton.setEnabled(false);
                mSlowButton.setText("Doing Slow Stuff");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("quick", mQuickButton.isEnabled());
        outState.putBoolean("slow", mSlowButton.isEnabled());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations() && dataFragment.mConnection != null)
        {
            getApplicationContext().unbindService(dataFragment.mConnection);
            dataFragment.mConnection = null;
        }
    }

    public void quickClicked(final View v)
    {
        mRequest = new BoundService.TestRequest(6, BoundService.TaskPriority.PRONTO, dataFragment);

        /** Defines callbacks for service binding, passed to bindService() */
        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder binder)
            {
                if (mRequest != null)
                {
                    // We've bound to LocalService, cast the IBinder and get BoundService instance
                    BoundService.LocalBinder localBinderinder = (BoundService.LocalBinder) binder;
                    BoundService service = localBinderinder.getService();
                    service.makeRequest(mRequest);
                    mRequest = null;
                    mQuickButton.setEnabled(false);
                    mQuickButton.setText("Doing Quick stuff");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0)
            {

            }
        };

        dataFragment.mConnection = connection;

        Intent intent = new Intent(this, BoundService.class);
        getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void slowClicked(View v)
    {
        mSlowButton.setEnabled(false);
        mSlowButton.setText("Doing Slow stuff");
        ModelObject.getInstance(this).doLongRunning(dataFragment);
    }

    @Override
    public void onRequestFinished()
    {
        getApplicationContext().unbindService(dataFragment.mConnection);
        dataFragment.mConnection = null;
        mQuickButton.setEnabled(true);
        mQuickButton.setText("Quick Operation");
    }

    @Override
    public void longRunningFinished()
    {
        mSlowButton.setEnabled(true);
        mSlowButton.setText("Slow Operation");
    }
}
