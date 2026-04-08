package com.pointlessapps.songbook.core.setlist.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import kotlinx.serialization.Serializable

@Entity(tableName = "setlists")
@Serializable
internal data class SetlistEntity(
    @PrimaryKey val id: Long,
    val name: String,
)

@Entity(
    tableName = "setlist_songs",
    primaryKeys = ["setlistId", "songId"],
    indices = [Index("setlistId"), Index("songId")],
    foreignKeys = [
        ForeignKey(
            entity = SetlistEntity::class,
            parentColumns = ["id"],
            childColumns = ["setlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class SetlistSongEntity(
    val setlistId: Long,
    val songId: Long,
)

internal data class SetlistWithSongs(
    @Embedded val setlist: SetlistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SetlistSongEntity::class,
            parentColumn = "setlistId",
            entityColumn = "songId",
        ),
    )
    val songs: List<SongEntity>,
)
