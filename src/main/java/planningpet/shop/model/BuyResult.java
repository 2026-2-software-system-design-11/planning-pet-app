package planningpet.shop.model;

/** 06 20번 buy의 결과. lack은 NOT_ENOUGH일 때 모자란 포인트이고 그 밖은 0이다. */
public record BuyResult(Outcome outcome, int lack) {
    public enum Outcome { BOUGHT, NOT_ENOUGH, ALREADY_OWNED }
}
