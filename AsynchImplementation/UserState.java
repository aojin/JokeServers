//import java.util.Random;
//import java.util.UUID;
//
//public class UserState {
//    UUID uuid;
//    String name;
//    boolean[] jokes = {false, false, false, false};
//    boolean[] proverbs = {false, false, false, false};
//
//    int jokeLastIndex;
//    int proverbLastIndex;
//
//    public UserState(String name, UUID uuid) {
//        this.name = name;
//        this.uuid = uuid;
//    }
//
//    public void markJokeAsRead(int readIndex){
//        jokes[readIndex] = true;
//    }
//
//    public void markProverbAsRead(int readIndex) {
//        proverbs[readIndex] = true;
//    }
//
//    public int getRandomIndex() {
//        Random rand = new Random();
//        return rand.nextInt(jokes.length);
//    }
//
//    public int getAnUnseenJokeIndex() {
//
//        if (checkJokesOrReset()){
//            resetToUnseen(jokes);
//        }
//
//        // Select a random index to test
//        int index = getRandomIndex();
//
//        while (jokes[index] == true) {
//            System.out.println("Joke at index "+ index +" has already been seen.");
//            index = getRandomIndex();
//        }
//        System.out.println("Marking joke at index " + index + " as seen.");
//        markJokeAsRead(index);
//        jokeLastIndex = index;
//        return  index;
//
//    }
//
//    public int getAnUnseenProverbIndex() {
//
//        if (checkProverbsOrReset()){
//            resetToUnseen(proverbs);
//        }
//
//        // Select a random index to test
//        int index = getRandomIndex();
//
//        while (proverbs[index] == true) {
//            System.out.println("Proverb at index "+ index +" has already been seen.");
//            index = getRandomIndex();
//        }
//        System.out.println("Marking joke at index " + index + " as seen.");
//        markProverbAsRead(index);
//        proverbLastIndex = index;
//        return  index;
//
//    }
//
//    public boolean checkJokesOrReset(){
//
//        for (int i = 0; i < jokes.length; i++){
//            // if we hit an unseen, we return false immediately
//            if (jokes[i] == false){
//                return false; // false, we didn't need to reset
//            }
//        }
//        return true;
//    }
//
//    public boolean  checkProverbsOrReset(){
//
//        for (int i = 0; i < proverbs.length; i++){
//            // if we hit an unseen, we return false immediately
//            if (proverbs[i] == false){
//                return false; // false, we didn't need to reset
//            }
//        }
//        return true;
//    }
//
//    public void resetToUnseen(boolean[] array) {
//
//        for(int i = 0; i < array.length; i++){
//            array[i] = false;
//        }
//
//        System.out.println("Resetting all to unseen...");
//
//    }
//
//    public String getName() {
//        return name;
//    }
//
//}
