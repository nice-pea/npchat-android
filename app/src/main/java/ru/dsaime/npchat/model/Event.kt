package ru.dsaime.npchat.model

sealed interface Event {
    class ParticipantAdded(
        val chat: Chat,
        val participant: Participant,
    ) : Event {
        companion object {
            const val NAME = "participant_added"
        }
    }

    class ParticipantRemoved(
        val chat: Chat,
        val participant: Participant,
    ) : Event {
        companion object {
            const val NAME = "participant_removed"
        }
    }

    class ChatUpdated(
        val chat: Chat,
    ) : Event {
        companion object {
            const val NAME = "chat_updated"
        }
    }

    class ChatCreated(
        val chat: Chat,
    ) : Event {
        companion object {
            const val NAME = "chat_created"
        }
    }
}
