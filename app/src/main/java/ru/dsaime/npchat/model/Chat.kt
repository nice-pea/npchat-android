package ru.dsaime.npchat.model

data class Chat(
    val id: String,
    val name: String,
    val chiefId: String,
)

data class Participant(
    val userId: String,
)

// type Chat struct {
//    ID      uuid.UUID // Уникальный ID чата
//            Name    string    // Название чата
//            ChiefID uuid.UUID // ID главного пользователя чата
//
//            Participants []Participant // Список участников чата
//            Invitations  []Invitation  // Список приглашений в чате
// }
