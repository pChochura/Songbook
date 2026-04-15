package com.pointlessapps.songbook.core.sync.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.time.Clock

@Entity(tableName = "sync_actions")
internal data class SyncActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncAction: SyncAction,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)

@Serializable
internal sealed interface SyncAction {
    @Keep
    @Serializable
    data class SaveSong(
        val song: Song,
        val setlistsIds: List<String>,
    ) : SyncAction

    @Keep
    @Serializable
    data class DeleteSong(
        val id: String,
    ) : SyncAction

    @Keep
    @Serializable
    data class AddSetlist(
        val setlist: Setlist,
    ) : SyncAction

    @Keep
    @Serializable
    data class DeleteSetlist(
        val id: String,
    ) : SyncAction

    @Keep
    @Serializable
    data class UpdateSetlistName(
        val id: String,
        val name: String,
    ) : SyncAction

    @Keep
    @Serializable
    data class UpdateSetlistSongs(
        val id: String,
        val songsIds: List<String>,
    ) : SyncAction

    @Keep
    @Serializable
    data class UpdateSongSetlists(
        val id: String,
        val setlistsIds: List<String>,
    ) : SyncAction

    @Keep
    @Serializable
    data class AddSongToSetlist(
        val setlistSongEntity: SetlistSongEntity,
    ) : SyncAction

    @Keep
    @Serializable
    data class RemoveSongFromSetlist(
        val setlistId: String,
        val songId: String,
    ) : SyncAction

    companion object {
        val SerializersModule = SerializersModule {
            polymorphic(SyncAction::class) {
                subclass(SaveSong::class, SaveSong.serializer())
                subclass(DeleteSong::class, DeleteSong.serializer())
                subclass(AddSetlist::class, AddSetlist.serializer())
                subclass(DeleteSetlist::class, DeleteSetlist.serializer())
                subclass(UpdateSetlistName::class, UpdateSetlistName.serializer())
                subclass(UpdateSetlistSongs::class, UpdateSetlistSongs.serializer())
                subclass(UpdateSongSetlists::class, UpdateSongSetlists.serializer())
                subclass(AddSongToSetlist::class, AddSongToSetlist.serializer())
                subclass(RemoveSongFromSetlist::class, RemoveSongFromSetlist.serializer())
            }
        }
    }
}
