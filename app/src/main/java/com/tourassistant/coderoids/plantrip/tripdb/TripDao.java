package com.tourassistant.coderoids.plantrip.tripdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface TripDao {
    @Query("SELECT * FROM TripEntity")
    List<TripEntity> getAllTrips();

    @Query("SELECT * FROM TripEntity WHERE id=:id")
    TripEntity getTripById(long id);

    @Insert
    long insertTrip(TripEntity tripEntity);

    @Delete
    void deleteTrip(TripEntity tripEntity);

    @Update(onConflict = REPLACE)
    void updateTrip(TripEntity tripEntity);
}
