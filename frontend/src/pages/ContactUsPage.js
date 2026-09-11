import { Link } from "react-router-dom";

export default function ContactUsPage() {
  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Contact Us</h1>
          <p className="muted">Tell us how we can help.</p>
        </div>
      </div>

      <div className="row">
        <div className="form-card panel">
          <h3>Get in touch</h3>
          <p>For support, partnership enquiries, and system access, contact our team:</p>
          <p>
            <strong>Email:</strong> support@qvs.example
          </p>
          <p>
            <strong>Phone:</strong> +263 24 000 0000
          </p>
          <p>
            <strong>Office:</strong> Harare, Zimbabwe
          </p>
        </div>

        <div className="form-card panel">
          <h3>We’re here to help</h3>
          <p>
            Whether you are an institution issuing credentials, an employer checking a record, or a learner needing
            guidance, our team can help with onboarding and support.
          </p>
          <Link className="button" to="/about">
            About us
          </Link>
        </div>
      </div>
    </div>
  );
}
