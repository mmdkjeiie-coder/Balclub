package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface MinuteDao {
    @Query("SELECT * FROM minutes ORDER BY date DESC")
    fun getAllMinutes(): Flow<List<Minute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMinute(minute: Minute)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY isPinned DESC, date DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: Notice)
}

@Database(entities = [User::class, Minute::class, Event::class, Notice::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun minuteDao(): MinuteDao
    abstract fun eventDao(): EventDao
    abstract fun noticeDao(): NoticeDao
}
