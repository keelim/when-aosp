import java.util.EnumMap;
import java.util.Map;

public enum AfterEnum { // 사용하려는 ENUM 값을 상수로 받는다?

    FIRST_APPLICATION_WINDOW(1),
    LAST_APPLICATION_WINDOW(99),
    TYPE_TOAST(2005),
    TYPE_INPUT_METHOD(2011),
    TYPE_WALLPAPER(2013),
    TYPE_DREAM(2023),
    TYPE_VOICE_INTERACTION(2031),
    TYPE_ACCESSIBILITY_OVERLAY(2032),
    TYPE_QS_DIALOG(2035);


    int value;

    AfterEnum(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }

    public static void main(String[] args) {
        Map<AfterEnum, Integer> map = new EnumMap<AfterEnum, Integer>(AfterEnum.class);
        int name = map.get(TYPE_ACCESSIBILITY_OVERLAY);
        System.out.println("name: "+ name);
    }
}

class CustomEnumMAp extends EnumMap<AfterEnum, Integer>{

    public CustomEnumMAp(Class<AfterEnum> keyType) {
        super(keyType);
    }


}
