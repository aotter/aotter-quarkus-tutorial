package model.po

import com.fasterxml.jackson.annotation.JsonFormat
import org.bson.Document
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

class ClamData: Document(){

    var id: ObjectId? = null

    var collectionName: String? = null
        set(value) {
            field = value
            put(ClamData::collectionName.name, value)
        }

    var authorId: String? = null
        set(value) {
            field = value
            put(ClamData::authorId.name, value)
        }

    var createTime: Instant? = null
        set(value) {
            field = value
            put(ClamData::createTime.name, value)
        }

    var lastModifiedTime: Instant? = null
        set(value) {
            field = value
            put(ClamData::lastModifiedTime.name, value)
        }

    var publishedTime: Instant? = null
        set(value) {
            field = value
            put(ClamData::publishedTime.name, value)
        }

    var state: State? = null
        set(value) {
            field = value
            put(ClamData::state.name, value?.name)
        }

    companion object {
        fun documentToClamData(document: Document): ClamData{
            val clamData = ClamData()
            clamData.collectionName = document.getString(ClamData::collectionName.name)
            clamData.authorId = document.getString(ClamData::authorId.name)
            clamData.createTime = dateToInstant(document.getDate(ClamData::createTime.name))
            clamData.lastModifiedTime = dateToInstant(document.getDate(ClamData::lastModifiedTime.name))
            clamData.publishedTime = dateToInstant(document.getDate(ClamData::publishedTime.name))
            clamData.state = document.getString(ClamData::state.name)?.let { State.valueOf(it) }
            document.keys.filterNot { ClamData::class.members.map { member -> member.name }.contains(it) }
                    .forEach { clamData.append(it, document.get(it)) }
            return clamData
        }

        private fun dateToInstant(date: Date?): Instant?{
            return date?.toInstant()
        }

    }

}

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class State {
    TEMP,
    PUBLISHED,
    ARCHIVED
}
