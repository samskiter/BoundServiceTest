package uk.co.airsource.android.common.examples.boundservicetest.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by sduke on 30/06/2014.
 */
public class ModelObject implements BoundService.TestRequest.RequestResultListener
{
    private static ModelObject sInstance;
    private final Context mContext;
    private ServiceConnection mConnection;
    private LongRunningListener mListener;

    private ModelObject(Context context)
    {
        mContext = context.getApplicationContext();
    }

    public static ModelObject getInstance(Context context)
    {
        if (sInstance == null)
        {
            sInstance = new ModelObject(context);
        }
        return sInstance;
    }

    public interface LongRunningListener
    {
        public void longRunningFinished();
    }

    public void doLongRunning(LongRunningListener listener)
    {
        mListener = listener;
        final BoundService.TestRequest request = new BoundService.TestRequest(40, BoundService.TaskPriority.MEH, this);

        /** Defines callbacks for service binding, passed to bindService() */
        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder binder)
            {
                if (request != null)
                {
                    // We've bound to LocalService, cast the IBinder and get BoundService instance
                    BoundService.LocalBinder localBinderinder = (BoundService.LocalBinder) binder;
                    BoundService service = localBinderinder.getService();
                    service.makeRequest(request);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0)
            {

            }
        };

        mConnection = connection;

        Intent intent = new Intent(mContext, BoundService.class);
        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onRequestFinished()
    {
        mContext.unbindService(mConnection);
        mConnection = null;
        if (mListener != null)
        {
            mListener.longRunningFinished();
        }
        mListener = null;
    }
}
