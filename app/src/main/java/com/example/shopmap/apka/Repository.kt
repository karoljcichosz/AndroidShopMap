
import android.content.Context
import android.os.AsyncTask
import androidx.room.Room.databaseBuilder
import com.example.shopmap.MyDB
import com.example.shopmap.Shop

class Repository(context: Context) {

    private val DB_NAME = "db_task"

    private val database: MyDB

    val shops: List<Shop>
        get() = database.ProductDAO().all

    init {
        database = databaseBuilder(context, MyDB::class.java!!, DB_NAME).build()
    }

    fun insert(vararg shops: Shop) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database.ProductDAO().insertAll(*shops)
                return null
            }
        }.execute()
    }

    fun update(shop: Shop) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                database.ProductDAO().update(shop)
                return null
            }
        }.execute()
    }

    fun delete(id: Int) {
        var product = get(id)
        if(product != null)
        {
            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg voids: Void): Void? {
                    database.ProductDAO().delete(product)
                    return null
                }
            }.execute()
        }
    }

    fun delete(shop: Shop) {
            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg voids: Void): Void? {
                    database.ProductDAO().delete(shop)
                    return null
                }
            }.execute()
    }

    fun get(id: Int): Shop {
        return database.ProductDAO().get(id)
    }
}