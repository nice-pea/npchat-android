package ru.dsaime.npchat.model

sealed interface Event {
    class ParticipantAdded(
        val chatId: String,
        val participant: Participant,
    ) : Event {
        companion object {
            const val NAME = "participant_added"
        }
    }

    class ParticipantRemoved(
        val chatId: String,
        val participant: Participant,
    ) : Event {
        companion object {
            const val NAME = "participant_removed"
        }
    }

    class ChatNameUpdated(
        val chatId: String,
        val name: String,
    ) : Event {
        companion object {
            const val NAME = "chat_name_updated"
        }
    }

    class ChatCreated(
        val chatId: String,
    ) : Event {
        companion object {
            const val NAME = "chat_created"
        }
    }
}
