package uk.co.airsource.android.common.examples.boundservicetest.app;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import java.util.Comparator;
import java.util.PriorityQueue;

public class BoundService extends Service
{
    // Binder given to clients
    private final IBinder mBinder    = new LocalBinder();

    private long stamp = 0;

    private CountTask mOnGoingTask = this.new CountTask();
    private TestRequest mOnGoingRequest;

    private PriorityQueue<TestRequest> mQueue = new PriorityQueue<TestRequest>(10,
            new Comparator<TestRequest>()
            {
                @Override
                public int compare(TestRequest request, TestRequest request2)
                {
                    if (request.mPriority.ordinal() > request2.mPriority.ordinal() ||
                            (request.mPriority == request2.mPriority &&
                                    request.timestamp < request2.timestamp))
                    {
                        return -1;
                    }
                    return 1;
                }
            }
    );

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder
    {
        BoundService getService()
        {
            // Return this instance of LocalService so clients can call public methods
            return BoundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        //cancel any ongoing
        if (mOnGoingTask != null)
        {
            mOnGoingTask.cancel(false);
        }
        return super.onUnbind(intent);
    }

    /** method for clients */
    public void makeRequest(TestRequest request)
    {
        request.timestamp = stamp++;
        mQueue.add(request);
        if (!cancelOnGoingIfNecessary())
        {
            startNextRequestIfNecessary();
        }
    }

    public void cancelRequest(TestRequest request)
    {
        mQueue.remove(request);
        if (request == mOnGoingRequest)
        {
            cancelOnGoingIfNecessary();
        }
    }

    private boolean cancelOnGoingIfNecessary()
    {
        if (mOnGoingRequest != null)
        {
            if (mQueue.peek().mPriority.ordinal() > mOnGoingRequest.mPriority.ordinal())
            {
                mOnGoingTask.cancel(false);
                return true;
            }
        }
        return false;
    }

    private void onGoingCancelled()
    {
        mQueue.add(mOnGoingRequest);
        startNextRequestIfNecessary();
    }

    private void onGoingFinished()
    {
        mOnGoingRequest.mResultListener.onRequestFinished();
        startNextRequestIfNecessary();
    }

    private void startNextRequestIfNecessary()
    {
        mOnGoingTask = null;
        mOnGoingRequest = null;
        if (mQueue.peek() != null)
        {
            mOnGoingRequest = mQueue.poll();
            mOnGoingTask = this.new CountTask();
            mOnGoingTask.execute(mOnGoingRequest.getWorkCount());
        }
    }

    private class CountTask extends AsyncTask<Integer, Integer, Long>
    {
        protected Long doInBackground(Integer... counts)
        {
            int count = counts[0];
            long totalSize = 0;
            for (int i = 0; i < count; i++)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                if (isCancelled()) break;
            }
            return totalSize;
        }

        protected void onPostExecute(Long result)
        {
            onGoingFinished();
        }

        @Override
        protected void onCancelled(Long aLong)
        {
            super.onCancelled(aLong);
            onGoingCancelled();
        }
    }

    public enum TaskPriority
    {
        MEH, PRONTO
    }

    public static class TestRequest
    {
        private final int mCount;
        private final TaskPriority mPriority;
        private final RequestResultListener mResultListener;
        public long timestamp = Long.MAX_VALUE;

        TestRequest(int count, TaskPriority priority, RequestResultListener resultListener)
        {
            mCount = count;
            mPriority = priority;
            mResultListener = resultListener;
        }

        int getWorkCount()
        {
            return mCount;
        }

        TaskPriority getPriority()
        {
            return mPriority;
        }

        public interface RequestResultListener
        {
            public void onRequestFinished();
        }
    }
}
