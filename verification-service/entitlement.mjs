const SKUS = new Set(['craftfolio_premium_monthly', 'craftfolio_premium_annual']);
export function entitlement(receipt, receiptId, now, allowTestReceipts = false) {
  if (receipt.receiptId !== receiptId || receipt.productType !== 'SUBSCRIPTION' ||
      !SKUS.has(receipt.productId) || (!allowTestReceipts && (receipt.testTransaction || receipt.betaProduct)) ||
      !Number.isFinite(receipt.purchaseDate) || receipt.purchaseDate > now) throw new Error('Invalid receipt');
  // RVS cancelDate is the loss-of-access date; stopping auto-renew does not revoke a paid term.
  const cancel = receipt.cancelDate;
  if (cancel != null && (!Number.isFinite(cancel) || cancel <= now)) return 0;
  return Math.min(now + 72 * 3600000, cancel ?? Infinity);
}
