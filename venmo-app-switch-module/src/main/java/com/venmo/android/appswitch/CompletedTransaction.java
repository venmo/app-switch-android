package com.venmo.android.appswitch;

import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.venmo.android.appswitch.TransactionRequest.TransactionType;

import java.util.Map;

public final class CompletedTransaction {

    private TransactionType mTransactionType;
    private Map<String, Double> mTargetsAndAmounts = Maps.newLinkedHashMap();
    private String mNote;

    private CompletedTransaction() {
        // only create instances in this class
    }

    public TransactionType getTransactionType() {
        return mTransactionType;
    }

    public Map<String, Double> getTargetsAndAmounts() {
        return mTargetsAndAmounts;
    }

    public String getNote() {
        return mNote;
    }

    public static class Builder {

        private CompletedTransaction mTransaction = new CompletedTransaction();

        public Builder note(String note) {
            mTransaction.mNote = note;
            return this;
        }

        public Builder transactionType(TransactionType type) {
            mTransaction.mTransactionType = type;
            return this;
        }

        public Builder recipient(String target, double amount) {
            mTransaction.mTargetsAndAmounts.put(target, amount);
            return this;
        }

        public CompletedTransaction build() {
            if (mTransaction.mTargetsAndAmounts.isEmpty()) {
                throw new IllegalStateException("CompletedTransaction has no targets or amounts");
            }

            if (TextUtils.isEmpty(mTransaction.mNote)) {
                throw new IllegalStateException("Note was not set");
            }

            Preconditions.checkNotNull(mTransaction.mTransactionType,
                    "Transaction type was not set");

            return mTransaction;
        }

    }
}
