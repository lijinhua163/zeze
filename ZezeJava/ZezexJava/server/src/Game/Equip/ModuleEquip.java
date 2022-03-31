package Game.Equip;

import Game.Fight.*;
import Zeze.Transaction.*;
import Game.*;
import Zeze.Net.Protocol;

//ZEZE_FILE_CHUNK {{{ IMPORT GEN
//ZEZE_FILE_CHUNK }}} IMPORT GEN


public final class ModuleEquip extends AbstractModule {
	public void Start(App app) {
		_tequip.getChangeListenerMap().AddListener(tequip.VAR_Items, new ItemsChangeListener());
	}

	public void Stop(App app) {
	}

	private static class ItemsChangeListener implements ChangeListener {
		private static String Name = "Game.Equip.Items";
		public static String getName() {
			return Name;
		}

		public final void OnChanged(Object key, Bean value) {
			// 记录改变，通知全部。
			BEquips bequips = (BEquips)value;

			SEquipement changed = new SEquipement();
			changed.Argument.setChangeTag(Game.Bag.BChangedResult.ChangeTagRecordChanged);
			changed.Argument.getItemsReplace().putAll(bequips.getItems());

			Game.App.Instance.Game_Login.getOnlines().SendReliableNotify((Long)key, getName(), changed);
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			// 增量变化，通知变更。
			@SuppressWarnings("unchecked")
			ChangeNoteMap2<Integer, Game.Bag.BItem> notemap2 = (ChangeNoteMap2<Integer, Game.Bag.BItem>)note;
			BEquips bequips = (BEquips)value;
			notemap2.MergeChangedToReplaced(bequips.getItems());

			SEquipement changed = new SEquipement();
			changed.Argument.setChangeTag(Game.Bag.BChangedResult.ChangeTagNormalChanged);

			changed.Argument.getItemsReplace().putAll(notemap2.getReplaced());
			for (var p : notemap2.getRemoved()) {
				changed.Argument.getItemsRemove().add(p);
			}

			Game.App.Instance.Game_Login.getOnlines().SendReliableNotify((Long)key, getName(), changed);
		}

		public final void OnRemoved(Object key) {
			SEquipement changed = new SEquipement();
			changed.Argument.setChangeTag(Game.Bag.BChangedResult.ChangeTagRecordIsRemoved);
			Game.App.Instance.Game_Login.getOnlines().SendReliableNotify((Long)key, getName(), changed);
		}
	}

	public int GetEquipPosition(int itemId) {
		return 0;
		// 如果装备可以穿到多个位置，则需要选择其中的一个位置返回。
		// 比如戒指，优先返回空的位置，都不为空（可能的规则）返回等级低的位置。
		// 如果物品不能装备到身上的话，返回错误(-1).
		//return -1;
	}
	// 装备只有装上取下两个操作，没有公开的需求，先不提供包装类了。

	@Override
	protected long ProcessEquipementRequest(Equipement rpc) throws Throwable {
		var session = Game.Login.Session.Get(rpc);

		Game.Bag.Bag bag = App.Game_Bag.GetBag(session.getRoleId().longValue());
		var bItem = bag.getItems().get(rpc.Argument.getBagPos());
		if (null != bItem) {
			int equipPos = GetEquipPosition(bItem.getId());
			if (equipPos < 0) {
				return ErrorCode(ResultCodeCannotEquip);
			}

			BEquips equips = _tequip.getOrAdd(session.getRoleId().longValue());
			Game.Bag.BItem bEquipAdd;
			var eItem = equips.getItems().get(equipPos);
			if (null != eItem) {
				// 装备目标位置已经存在装备，交换。
				// 先都删除，这样就能在原位置上交换的装备，否则对于包裹可能加到其他位置。
				equips.getItems().remove(equipPos);
				bag.Remove(rpc.Argument.getBagPos(), bItem.getId(), 1);

				Game.Bag.BItem tempVar = new Game.Bag.BItem();
				tempVar.setId(eItem.getId());
				tempVar.setNumber(1);
				tempVar.setExtra(eItem.getExtra_Game_Equip_BEquipExtra().Copy());
				bag.Add(rpc.Argument.getBagPos(), tempVar);

				bEquipAdd = new Game.Bag.BItem();
				bEquipAdd.setId(bItem.getId());
				bEquipAdd.setNumber(1);
				bEquipAdd.setExtra(bItem.getExtra_Game_Equip_BEquipExtra().Copy());
				equips.getItems().put(equipPos, bEquipAdd);
			}
			else {
				// 装备目标位置为空
				bag.Remove(rpc.Argument.getBagPos(), bItem.getId(), 1);
				bEquipAdd = new Game.Bag.BItem();
				bEquipAdd.setId(bItem.getId());
				bEquipAdd.setNumber(1);
				bEquipAdd.setExtra(bItem.getExtra_Game_Equip_BEquipExtra().Copy());
				equips.getItems().put(equipPos, bEquipAdd);
			}
			session.SendResponse(rpc);
			return Procedure.Success;
		}
		return ErrorCode(ResultCodeItemNotFound);
	}

	@Override
	protected long ProcessUnequipementRequest(Unequipement rpc) throws Throwable {
		var session = Game.Login.Session.Get(rpc);

		BEquips equips = _tequip.getOrAdd(session.getRoleId().longValue());
		var eItem = equips.getItems().get(rpc.Argument.getEquipPos());
		if (null != eItem) {
			equips.getItems().remove(rpc.Argument.getEquipPos());
			var bag = App.Game_Bag.GetBag(session.getRoleId().longValue());
			var bItemAdd = new Game.Bag.BItem();
			bItemAdd.setId(eItem.getId());
			bItemAdd.setNumber(1);
			bItemAdd.setExtra((BEquipExtra)eItem.getExtra().CopyBean());
			if (0 != bag.Add(-1, bItemAdd)) {
				return ErrorCode(ResultCodeBagIsFull); // bag is full
			}
			session.SendResponse(rpc);
			return Procedure.Success;
		}

		return ErrorCode(ResultCodeEquipNotFound);
	}

	public Game.Item.Item GetEquipItem(long roleId, int position) {
		BEquips equips = _tequip.getOrAdd(roleId);
		return GetEquipItem(equips, position);
	}

	public Game.Item.Item GetEquipItem(BEquips equips, int position) {
		var equip = equips.getItems().get(position);
		if (null != equip) {
			var extraTypeId = equip.getExtra().getTypeId();
			if (extraTypeId == BEquipExtra.TYPEID)
				return new Equip(equip, (BEquipExtra)equip.getExtra().getBean());
			throw new RuntimeException("unknown extra");
		}
		return null;
	}

	public void CalculateFighter(Fighter fighter) {
		if (fighter.getId().getType() != BFighterId.TypeRole) {
			return;
		}

		BEquips equips = _tequip.getOrAdd(fighter.getId().getInstanceId());
		for (var pos : equips.getItems().keySet()) {
			GetEquipItem(equips, pos).CalculateFighter(fighter);
		}
	}

	// ZEZE_FILE_CHUNK {{{ GEN MODULE
    public ModuleEquip(Game.App app) {
        super(app);
    }
	// ZEZE_FILE_CHUNK }}} GEN MODULE
}