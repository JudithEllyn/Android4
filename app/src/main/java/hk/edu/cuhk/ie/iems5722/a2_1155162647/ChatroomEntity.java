package hk.edu.cuhk.ie.iems5722.a2_1155162647;

    public class ChatroomEntity{
        private int id;
        private String name;
        public ChatroomEntity(int id,String name){
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString(){
            return name;
        }
        public int getId(){return this.id;};
        public String getName(){return this.name;};
    }
