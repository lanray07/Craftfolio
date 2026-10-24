import {test} from 'node:test';
import assert from 'node:assert/strict';
import {entitlement} from './entitlement.mjs';
const receipt = {receiptId: 'r', productType: 'SUBSCRIPTION', productId: 'craftfolio_premium_monthly', purchaseDate: 500, cancelDate: null};
test('valid receipt has bounded offline lease', () => assert.equal(entitlement(receipt, 'r', 1000), 1000 + 72 * 3600000));
test('cancelled and expired subscriptions are denied', () => assert.equal(entitlement({...receipt, cancelDate: 900}, 'r', 1000), 0));
test('future cancellation preserves paid term', () => assert.equal(entitlement({...receipt, cancelDate: 2000}, 'r', 1000), 2000));
test('past renewal alone does not mean access lost', () => assert.ok(entitlement({...receipt, renewalDate: 900}, 'r', 1000) > 1000));
test('wrong SKU, receipt, future purchase and test receipts are rejected', () => {
  for (const change of [{productId:'other'}, {receiptId:'wrong'}, {purchaseDate:2000}, {testTransaction:true}]) assert.throws(() => entitlement({...receipt, ...change}, 'r', 1000));
});
test('separate test deployment can explicitly accept Amazon LAT or sandbox receipts', () => {
  const testReceipt={...receipt,betaProduct:true,testTransaction:true};
  assert.throws(() => entitlement(testReceipt,'r',1000));
  assert.ok(entitlement(testReceipt,'r',1000,true)>1000);
});
