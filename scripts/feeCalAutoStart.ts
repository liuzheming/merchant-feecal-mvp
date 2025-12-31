type StartRequest = {
  merchantType: string;
  merchantCode: string;
  requestId?: string;
};

const url = "http://localhost:8080/web/feeCal/summary/autoStart";

const payload: StartRequest = {
  merchantType: "MERCHANT",
  merchantCode: "M0001",
  requestId: "req-auto-001",
};

async function main() {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  const text = await res.text();
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}: ${text}`);
  }
  console.log(text);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
