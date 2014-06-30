package uk.co.airsource.android.common.examples.boundservicetest.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.ServiceConnection;
import android.os.Bundle;

public class RetainedFragment extends Fragment implements BoundService.TestRequest.RequestResultListener,
                                                          ModelObject.LongRunningListener
{
    // data object we want to retain
    public ServiceConnection mConnection;
    private boolean cachedResult = false;
    private boolean longCachedResult = false;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity)
    {
        if (cachedResult)
        {
            ((MainActivity) activity).onRequestFinished();
            cachedResult = false;
        }
        if (longCachedResult)
        {
            ((MainActivity) activity).longRunningFinished();
            longCachedResult = false;
        }
        super.onAttach(activity);
    }

    @Override
    public void onRequestFinished()
    {
        if (getActivity() != null)
        {
            ((MainActivity)getActivity()).onRequestFinished();
        }
        else
        {
            cachedResult = true;
        }
    }

    @Override
    public void longRunningFinished()
    {
        if (getActivity() != null)
        {
            ((MainActivity)getActivity()).longRunningFinished();
        }
        else
        {
            longCachedResult = true;
        }
    }
}
