package com.ashencrown.model;

public class Weapon {

    private final String    name;
    private final WeaponType type;
    private final int       might;
    private final int       hit;
    private final int       crit;
    private final int       minRange;
    private final int       maxRange;
    private int             uses;
    private final boolean   magical;

    public Weapon(String name, WeaponType type, int might, int hit, int crit,
                  int minRange, int maxRange, int uses) {
        this.name     = name;
        this.type     = type;
        this.might    = might;
        this.hit      = hit;
        this.crit     = crit;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.uses     = uses;
        this.magical  = (type == WeaponType.ANIMA || type == WeaponType.LIGHT
                      || type == WeaponType.DARK  || type == WeaponType.STAFF);
    }

    // ── Armas físicas ──────────────────────────────────────────────────────────
    public static Weapon ironSword()   { return new Weapon("Espada de Hierro",  WeaponType.SWORD, 5, 90, 0, 1, 1, 46); }
    public static Weapon steelSword()  { return new Weapon("Espada de Acero",   WeaponType.SWORD, 8, 75, 0, 1, 1, 30); }
    public static Weapon ironLance()   { return new Weapon("Lanza de Hierro",   WeaponType.LANCE, 6, 80, 0, 1, 1, 45); }
    public static Weapon steelLance()  { return new Weapon("Lanza de Acero",    WeaponType.LANCE, 9, 70, 0, 1, 1, 30); }
    public static Weapon silverLance() { return new Weapon("Lanza de Plata",    WeaponType.LANCE,12, 75, 0, 1, 1, 20); }
    public static Weapon ironAxe()     { return new Weapon("Hacha de Hierro",   WeaponType.AXE,  8, 70, 0, 1, 1, 45); }
    public static Weapon steelAxe()    { return new Weapon("Hacha de Acero",    WeaponType.AXE, 11, 60, 0, 1, 1, 30); }
    public static Weapon ironBow()     { return new Weapon("Arco de Hierro",    WeaponType.BOW,  6, 85, 0, 2, 2, 45); }
    public static Weapon steelBow()    { return new Weapon("Arco de Acero",     WeaponType.BOW,  9, 70, 0, 2, 2, 30); }

    // ── Armas mágicas ──────────────────────────────────────────────────────────
    public static Weapon fire()        { return new Weapon("Fuego",   WeaponType.ANIMA, 5, 90, 5, 1, 2, 40); }
    public static Weapon thunder()     { return new Weapon("Trueno",  WeaponType.ANIMA, 6, 80, 5, 1, 2, 35); }
    public static Weapon flux()        { return new Weapon("Flujo",   WeaponType.DARK,  7, 80, 5, 1, 2, 45); }
    public static Weapon heal()        { return new Weapon("Sanar",   WeaponType.STAFF, 0,100, 0, 1, 1, 30); }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String     getName()     { return name; }
    public WeaponType getType()     { return type; }
    public int        getMight()    { return might; }
    public int        getHit()      { return hit; }
    public int        getCrit()     { return crit; }
    public int        getMinRange() { return minRange; }
    public int        getMaxRange() { return maxRange; }
    public int        getUses()     { return uses; }
    public boolean    isMagical()   { return magical; }
    public void       use()         { if (uses > 0) uses--; }
}
