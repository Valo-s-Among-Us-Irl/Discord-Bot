package valoeghese.amongusirl;

class TestIDFunc {
	public static void main(String[] args) {
		int i = Util.getIdCode(Task.TRANSFER_DATA, Room.ELECTRICAL);
		System.out.println(Util.getTask(i));
		System.out.println(Util.getRoom(i));
		System.out.println(i);
	}
}
