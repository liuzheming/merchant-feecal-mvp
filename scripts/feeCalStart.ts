type StartRequest = {
  merchantType: string;
  merchantCode: string;
};

const url = "http://localhost:8080/web/feeCal/summary/start";
const merchantType = "MERCHANT";
const merchantCode = "M0001";

const payload: StartRequest = {
  merchantType,
  merchantCode,
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
