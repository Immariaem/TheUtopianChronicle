package study.coco.project_code

// handles npc dialogue, guardian trial and mirror labyrinth puzzle
class DialogueHandler(state: GameState, world: World) : BaseHandler(state, world) {

    // finds and returns the appropriate npc dialogue
    fun talk(target: String): String {
        if (target.isBlank()) return "Talk to whom? Try: talk <name>"

        val npc = state.currentQuadrant.visibleObjects.npcs
            .find { it.npcName.lowercase() == target.lowercase() && it.isActive }
            ?: return "There's no one called '$target' here."

        if (npc.npcId == "pearl_guardian" && "relics_offered" in state.gameFlags &&
            "guardian_trial_complete" !in state.gameFlags) {
            if (state.currentGuardianQuestion == 0) {
                state.guardianAnswersCorrect = true
                return "\"The relics are accepted. Now I will ask you three questions.\"\n\n\"Many have come seeking this island. What drives you to follow in their footsteps?\"\n\nA) I am following someone who came this way. I need to know what happened to them.\n\nB) I seek a perfect life, free from the pain of the world above.\n\nC) I will succeed where others have failed. That is reason enough."
            }
            return when (state.currentGuardianQuestion) {
                1 -> "\"Some call the island a utopia. What do you believe a world without suffering would cost?\"\n\nA) Nothing. Peace is not something that must be paid for.\n\nB) Everything that makes life worth living.\n\nC) Only the freedom of those too weak to endure it."
                2 -> "\"If you reached the island and found nothing you hoped for, what would you do?\"\n\nA) I would stay. Any destination is better than the journey.\n\nB) I would leave and carry the truth back with me.\n\nC) I would make it into what I needed it to be."
                else -> "\"Answer truthfully, seeker.\""
            }
        }

        // talking to the smiling ones unlocks the path to the secluded beach
        if (npc.npcId == "smiling_ones" && "met_smiling_ones" !in state.gameFlags) {
            state.gameFlags.add("met_smiling_ones")
        }

        // talking to father for the first time unlocks the labyrinth
        if (npc.npcId == "father" && "found_father" !in state.gameFlags) {
            state.gameFlags.add("found_father")
        }

        // cargo delivery: talking to Zara with cargo in inventory delivers it
        if (npc.npcId == "captain_zara" && "cargo_delivered" !in state.gameFlags) {
            val cargo = state.playerInventory.find { it.itemId == "zaras_cargo" }
            if (cargo != null) {
                state.playerInventory.remove(cargo)
                state.gameFlags.add("cargo_delivered")
            }
        }

        val conditionalDialogue = npc.dialogueConditions.firstOrNull { it.flag in state.gameFlags }
        return conditionalDialogue?.text ?: npc.dialogue
    }

    // processes player answers for the pearl guardian trial
    fun answerGuardian(choice: String): String {
        if (state.currentQuadrant.quadrantId != "H8" || "relics_offered" !in state.gameFlags)
        {
            return "Unknown command: $choice. Type 'help' for a list of commands."
        }

        val correctAnswers = listOf("a", "b", "b")

        if (choice != correctAnswers[state.currentGuardianQuestion]) {
            state.guardianAnswersCorrect = false
        }
        state.currentGuardianQuestion++

        if (state.currentGuardianQuestion == 1) {
            return "\"Some call the island a utopia. What do you believe a world without suffering would cost?\"\n\nA) Nothing. Peace is not something that must be paid for.\n\nB) Everything that makes life worth living.\n\nC) Only the freedom of those too weak to endure it."
        }

        if (state.currentGuardianQuestion == 2) {
            return "\"If you reached the island and found nothing you hoped for, what would you do?\"\n\nA) I would stay. Any destination is better than the journey.\n\nB) I would leave and carry the truth back with me.\n\nC) I would make it into what I needed it to be."
        }

        if (state.guardianAnswersCorrect) {
            state.gameFlags.add("guardian_trial_complete")
            return "The Pearl Guardian nods slowly. \"You have answered well, seeker. Surface to the west and head for the desert. Cross it heading west. The Island of Bliss lies beyond the far shore.\""
        } else {
            state.currentGuardianQuestion = 0
            state.guardianAnswersCorrect = true
            return "The Pearl Guardian shakes his head. \"Your answers do not ring true. Speak to me again when you are ready.\""
        }
    }

    // processes player answers inside the mirror labyrinth
    fun answerLabyrinth(choice: String): String {
        if (!state.labyrinthActive) return "Unknown command: $choice. Type 'help' for a list of commands."

        val correctAnswers = listOf("a", "b", "a", "b", "b", "c")
        val fatherTexts = listOf(
            "Your father's reflection speaks quietly beside yours: \"Because I was afraid that ordinary was all I would ever be. And I couldn't face that.\"",
            "Your father's reflection speaks: \"Three years. Your childhood. The chance to watch you grow. I didn't know I was paying until it was already gone.\"",
            "Your father's reflection speaks: \"I thought I was. Until I realised I hadn't felt anything in months. Not worry. Not longing. Not love. Nothing.\"",
            "Your father's reflection speaks: \"I watched it happen slowly. First the worry. Then the longing. Then the memories. Then the names of the people they loved.\"",
            "Your father's reflection speaks: \"Ask me again when I've had a chance to be imperfect for a while.\""
        )

        if (choice != correctAnswers[state.labyrinthQuestion]) {
            state.labyrinthActive = false
            state.labyrinthQuestion = 0
            state.labyrinthAwaitingMove = false
            return "The mirrors cloud over. The corridor behind you dissolves. You find yourself back at the entrance, your father beside you.\n\n\"Again,\" he says quietly. \"We face it again.\"\n\nUse the archway to enter once more."
        }

        if (state.labyrinthQuestion == 5) {
            state.labyrinthActive = false
            state.labyrinthQuestion = 0
            state.labyrinthAwaitingMove = false
            state.gameFlags.add("mirror_labyrinth_complete")

            val j3 = world.quadrants.find { it.quadrantId == "J3" }
            if (j3 != null) {
                state.currentQuadrant = j3
                state.visitedQuadrants.add(j3.quadrantId)
            }
            return "The mirrors shatter.\n\nNot with violence, with release. Every reflection, every version of you and your father, dissolves into light.The labyrinth opens. The western wall simply isn't there anymore.\n\nYou and your father walk out together.\n\n${j3?.description?.initial ?: ""}"
        }

        val fatherText = fatherTexts[state.labyrinthQuestion]
        state.labyrinthQuestion++
        state.labyrinthAwaitingMove = true
        return "$fatherText\n\nThe path ahead clears. Move deeper."
    }

    // returns corridor text and the next question based on current progress
    fun nextLabyrinthQuestion(): String {
        val corridorTexts = listOf(
            "The mirror clears. Your reflections multiply as you walk deeper. Another intersection, another reflection waiting.",
            "The corridor shifts around you. Behind you, the entrance has disappeared. There is only forward.",
            "The air grows heavier. Your father walks beside you in silence. The reflections track your every step.",
            "The labyrinth breathes. The reflections watch you both with something that might be recognition.",
            "One final corridor. The western wall shimmers ahead. The last reflection steps forward."
        )

        val questionTexts = listOf(
            "\"What does a perfect world cost?\"\n\nA) The suffering of those who cannot enter it.\nB) Everything that makes us human.\nC) Only what we are willing to give.",
            "\"Could you be happy here?\"\n\nA) No. Happiness without struggle is not happiness.\nB) Yes. The peace is real. The beauty is real.\nC) Perhaps. If I forgot enough.",
            "\"What did the Smiling Ones lose?\"\n\nA) Nothing. They found what they were looking for.\nB) Everything that made them themselves.\nC) Only what caused them pain.",
            "\"Is the search for perfection worth pursuing?\"\n\nA) Yes. The pursuit itself makes us better.\nB) No. It blinds us to what we already have.\nC) Only if we accept we may never arrive.",
            "\"What is utopia?\"\n\nA) The world we build when we refuse to accept suffering.\nB) A destination that demands more than we can pay.\nC) A beautiful lie we tell ourselves to avoid living."
        )

        val idx = state.labyrinthQuestion - 1
        return "${corridorTexts[idx]}\n\n${questionTexts[idx]}"
    }

}


