/** @type {import('next').NextConfig} */
const nextConfig = {
  // CI runs `npm run lint` explicitly. Next 15's legacy build-time lint invocation
  // is incompatible with the project's flat ESLint configuration.
  eslint: { ignoreDuringBuilds: true },
};

export default nextConfig;
