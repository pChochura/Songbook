package com.pointlessapps.songbook.core.setlist.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "setlists")
@Serializable
internal data class SetlistEntity(
    @PrimaryKey val id: String,
    val name: String,
)

@Entity(
    tableName = "setlist_songs",
    primaryKeys = ["setlist_id", "song_id"],
    indices = [Index("setlist_id"), Index("song_id")],
    foreignKeys = [
        ForeignKey(
            entity = SetlistEntity::class,
            parentColumns = ["id"],
            childColumns = ["setlist_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
@Serializable
internal data class SetlistSongEntity(
    @ColumnInfo("setlist_id")
    @SerialName("setlist_id")
    val setlistId: String,
    @ColumnInfo("song_id")
    @SerialName("song_id")
    val songId: String,
    @ColumnInfo(defaultValue = "0")
    val order: Int,
)

internal data class SetlistWithCount(
    @Embedded
    val setlist: SetlistEntity,
    val songCount: Int,
)
