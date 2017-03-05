package de.wwu.muggl.solvers.solver.constraints;

/**
 * Interface for all numeric constraints.
 * @author Christoph Lembeck
 */
public interface NumericConstraint extends SingleConstraint{

    /**
     * Returns the polynomial that is stored inside the numeric constraint.
     * Numeric constraints have always the form
     * a<sub>1</sub>m<sub>1</sub>+...+a<sub>n</sub>m<sub>n</sub>+c
     * &lt;<i>op</i>&gt; 0 whereas &lt;<i>op</i>&gt; is one of the operators
     * &quot;==&quot;, &quot;&lt;&quot; , or &quot;&lt;=&quot; the m<sub>i</sub>
     * are any monomials, and c is any numeric constant.
     * @return the polynomial inside the constraint.
     */
    public Polynomial getPolynomial();

}
