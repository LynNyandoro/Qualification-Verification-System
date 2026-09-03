import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import VerifyPage from "./pages/VerifyPage";
import api from "./api";

jest.mock("./api");

test("verify page posts a code and shows a valid outcome", async () => {
  api.post.mockResolvedValue({
    data: {
      result: "VALID",
      message: "Qualification is authentic and currently active.",
      hashMatch: true,
      qualification: {
        title: "Master of Information Systems Management",
        holderName: "Amina Chikomo",
        issuingInstitution: "Midlands State University",
        credentialHash: "abc",
      },
    },
  });

  render(<VerifyPage />);
  fireEvent.click(screen.getByText(/Verify authenticity/i));

  await waitFor(() => expect(screen.getByText(/authentic and currently active/i)).toBeInTheDocument());
    expect(api.post).toHaveBeenCalledWith(
    "/verify",
    expect.objectContaining({ verificationCode: "QVS-DEMO12345" })
  );
});
