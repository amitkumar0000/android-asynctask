# android-asynctask

AsyncTask Internal Mechanism

AsyncTask works on workerQueue Model.

In case of execute ,
    AsyncTask will create one Thread that keep on reading from queue sequentially.
    So even 1000 async task is created. all will be executed on sequence way.

In Case of executeOnExecutor
    More than one thread is created but all thread get work from queue.
    so it will faster than execute case.

