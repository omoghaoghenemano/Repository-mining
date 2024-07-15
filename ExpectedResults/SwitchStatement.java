public class SwitchStatement {

    public void switchStatement() {
        int x = 2;
        var y = 1;
        switch (x) {
            case 1:
                x = x + 1;
            case 2:
                x = x + 2;
                break;
            case 3:
                x = x + 3;
            default:
                x = x + 4;
        }
        x = x % y;
    }

}