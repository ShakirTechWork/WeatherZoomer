package com.shakir.weatherzoomer.workManager

//import androidx.work.Worker
//import androidx.work.WorkerParameters


private const val TAG = "AppWorker"
//class AppWorker(private val context: Context, params: WorkerParameters): Worker(context, params) {
//    override fun doWork(): Result {
//        Log.d(TAG, "doWorkCalled: worker_called")
//        val repository = (context as Application).appRepository
//        CoroutineScope(Dispatchers.IO).launch {
////            val userLocation = repository.userCurrentLocation.collect().toString()
////            val response = repository.getForecastData("worli",1, "no", "yes")
////            if (response.isSuccessful && response.body() != null) {
////                response.body()?.let { body ->
////                    if (body.alerts.alert.isNotEmpty()) {
////                        val alert = body.alerts.alert[0]
////                        showNotification(alert.event, alert.headline)
////                    }
////                }
////            }
//        }
//        showNotification("alert.event", "alert.headline")
//        return Result.success()
//    }
//
//    private fun showNotification(title: String, message: String) {
//        val intent = Intent(context, MainActivity::class.java)
//        intent.putExtra("fragment_key", "your_fragment_identifier") // Pass any data needed for navigation
//
//        val pendingIntent = PendingIntent.getActivity(
//            context,
//            0,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
////        val largeIconBitmap =
////            BitmapFactory.decodeResource(context.resources, R.drawable.warning_svgrepo_com__1_)
//
//
//        val notification = NotificationCompat.Builder(context, Application.CHANNEL_ID)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setLargeIcon(BitmapFactory. decodeResource (context.resources , R.drawable. triangle_warning ))
//            .setSmallIcon(R.drawable.baseline_water_drop_16)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .build()
//
//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        notificationManager.notify(1, notification)
//    }
//
//}