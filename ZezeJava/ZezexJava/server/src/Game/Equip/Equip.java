package Game.Equip;

public class Equip extends Game.Item.Item {

	public Equip(Game.Equip.BItem bItem, BEquipExtra extra) {
		super(bItem);

	}

	@Override
	public void CalculateFighter(Game.Fight.Fighter fighter) {
		fighter.getBean().setAttack(fighter.getBean().getAttack() + 20.0f);
		fighter.getBean().setDefence(fighter.getBean().getDefence() + 20.0f);
	}

	@Override
	public boolean Use() {
		return false;
	}
}
