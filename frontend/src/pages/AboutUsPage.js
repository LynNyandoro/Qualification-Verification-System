import { Link } from "react-router-dom";

export default function AboutUsPage() {
  return (
    <div>
      <div className="page-head">
        <div>
          <h1>About Us</h1>
          <p className="muted">Building trust in education and skills recognition across Zimbabwe.</p>
        </div>
      </div>

      <div className="panel form-card">
        <h2>Our mission</h2>
        <p>
          The Qualification Verification System helps institutions, employers, and learners verify credentials in a
          secure, transparent, and efficient way.
        </p>
        <p>
          We make it easier to confirm academic records, reduce fraud, and support faster decision-making for
          recruitment, admissions, and professional recognition.
        </p>
      </div>

      <div className="form-card panel">
        <h3>Our team</h3>
        <ul>
          <li>Lyn Nyandoro</li>
          <li>Tatenda Benjamin</li>
          <li>Burton Mareke</li>
          <li>Tatenda Nyamhunga</li>
        </ul>
      </div>

      <div className="row">
        <div className="form-card panel">
          <h3>What we do</h3>
          <ul>
            <li>Record qualification data in a central, auditable system.</li>
            <li>Issue and track credentials for students and institutions.</li>
            <li>Let employers and verifiers validate authenticity quickly.</li>
          </ul>
        </div>
        <div className="form-card panel">
          <h3>Why it matters</h3>
          <p>
            Verified qualifications strengthen trust in education, improve hiring confidence, and protect institutions
            from forged or misrepresented records.
          </p>
          <Link className="button" to="/contact">
            Contact us
          </Link>
        </div>
      </div>
    </div>
  );
}
