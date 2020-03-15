class Rule {
    String leftHandSide;
    String rightHandSide;

    def static make(Closure closure) {
        Rule rule = new Rule()
        closure.delegate = rule
        closure()
        println rule.leftHandSide + " -> " + rule.rightHandSide
    }

    def because(String becauseClause) {
        this.leftHandSide = becauseClause
    }

    def then(String thenClause){
        this.rightHandSide = thenClause
    }

    static void main(String[] args) {
        Rule.make{
            because "N < 5"
            then "good"
        }
    }

}

