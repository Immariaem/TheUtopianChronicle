# The Utopian Chronicle

Your father was a writer and explorer, obsessed with a legend: the **Island of Bliss**, a utopia south beyond the ocean where no suffering exists and every day is perfect. He spent years researching it, filling a leather-bound book he called *The Utopian Chronicle* with maps, travel accounts and theories. Then, three years ago, he left without the book. He never came back.

Now you hold his unfinished chronicle. You add your own words to the last page:

*"I'm coming to find you. To finish what you started. To bring you home."*

---

## Running the Game

**Build and run from source:**
```
./gradlew clean package
java -jar build/libs/text-adventure.jar
```

**Then open your browser and go to:**
```
http://localhost:8080
```

---

## Commands

| Command | Action |
|---|---|
| `n` / `s` / `e` / `w` | move north, south, east, west |
| `t <object>` | take an object |
| `d <object>` | drop an object |
| `u <object>` | use an object |
| `i` | show your inventory |
| `l` | look around |
| `x <object>` | examine something closely |
| `talk <name>` | talk to someone |
| `h` | show all commands |
| `q` | quit and restart |

---

## Multiple Players

Each browser session gets its own independent game instance, multiple people can play at the same time without interfering with each other.
