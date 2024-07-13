public class SwitchStatement {

    public void switchStatement() {
        int x;
        var y = 1;
        switch (x) {
            case 1:
                a(x);
            case 2:
                b(y);
                break;
            case 3:
                c(x);
            default:
                d(y);
        }
        e(x % y);
    }

}