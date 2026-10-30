package com.craisinlord.antarchy.content.worldgen.elythia.reef;

interface ReefPiece {
    int centerX();

    int centerZ();

    int radius();

    boolean covers(int x, int z, double margin);

    void render(ReefCanvas canvas);

    static double segmentDistance(double px, double pz, double ax, double az, double bx, double bz) {
        double dx = bx - ax;
        double dz = bz - az;
        double lengthSquared = dx * dx + dz * dz;
        double t = lengthSquared <= 0.0D ? 0.0D : ((px - ax) * dx + (pz - az) * dz) / lengthSquared;
        t = Math.max(0.0D, Math.min(1.0D, t));
        double cx = ax + dx * t - px;
        double cz = az + dz * t - pz;
        return Math.sqrt(cx * cx + cz * cz);
    }
}
