import android.content.Context
import androidx.room.Room
import com.example.cropchecker.database.AppDatabase

object DatabaseProvider{
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "prediction_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
