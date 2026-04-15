/*
 * Copyright (c) 2026. Tim Langhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.jpangolin.friendlyimp.ioimp.cmd.linux;

import eu.jpangolin.friendlyimp.ioimp.cmd.AbstractNativeCommand;
import eu.jpangolin.friendlyimp.ioimp.cmd.INativeCommandOption;
import eu.jpangolin.friendlyimp.ioimp.cmd.INativeCommandResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.jpangolin.friendlyimp.ioimp.cmd.linux.LsblkCommand.LsblkOutputOptionArg.*;

/**
 * Launch the {@code lsblk} command.
 * <p>
 * More infos about can be found <a href="https://www.linux.org/docs/man8/lsblk.html">here</a>.
 * On the terminal type {@code lsblk -h} for more information.
 * </p>
 */
public final class LsblkCommand extends AbstractNativeCommand<LsblkCommand.Lsblk> {

    /**
     * Regular Expression to parse the columns of the 'lsblk' command.
     */
    private static final String LSBLK_REG = """
            ^TYPE="(?<type>[a-z]+?)"\\s
             TRAN="(?<tran>[a-z]*?)"\\s
             FSTYPE="(?<fstype>[a-z0-9]*?)"\\s
             FSAVAIL="(?<fsavail>[0-9]*?)"\\s
             SIZE="(?<size>[0-9]*?)"\\s
             MOUNTPOINT="(?<mountpoint>[^"]*?)"\\s
             SERIAL="(?<serial>[0-9A-Z]*?)"\\s
             NAME="(?<name>[a-z0-9]*?)"\\s
             LABEL="(?<label>[a-zA-Z0-9 ]*?)"\\s
             UUID="(?<uuid>[a-zA-Z0-9-]*?)"$
            """;
    /// Lsblk output pattern
    private static final Pattern LSBLK_CMD_PATTERN = Pattern.compile(LSBLK_REG);

    /// Default command option which is parable with the {@linkplain #LSBLK_REG lsblk regex}.
    private static final String[] DEFAULT_CMD = {
            LsblkOption.ASCII.fullArg(),
            LsblkOption.PAIRS.fullArg(),
            LsblkOption.BYTES.fullArg(),
            LsblkOption.OUTPUT.fullArg(),
            Stream.of(TYPE, TRAN, FS_TYPE, FS_AVAILABLE, SIZE, MOUNTPOINT, SERIAL, NAME, LABEL, UUID)
                    .map(Supplier::get)
                    .collect(Collectors.joining(","))
    };

    /// Option for the command.
    ///
    /// from the output of `lsblk -h`:
    ///
    ///   - `-A, --noempty`             keine leeren Geräte ausgeben
    ///   - `-D, --discard`             Verwerfungs-Capabilities ausgeben
    ///   - `-E, --dedup` <column>      Ausgabe nach <column> de-duplizieren
    ///   - `-I, --include` <Liste>     nur Geräte mit angegebenen Major-Nummern anzeigen
    ///   - `-J, --json`                im JSON-Format ausgeben
    ///   - `-M, --merge`               gruppiert die Elterneinträge von Unterbäumen (verwendbar für RAIDs, Multi-Pfad)
    ///   - `-O, --output-all`          alle Spalten ausgeben
    ///   - `-P, --pairs`          Ausgabeformat Schlüssel="Wert" verwenden
    ///   - `-S, --scsi`           Information zu SCSI-Geräten ausgeben
    ///   - `-T, --tree[=<Spalte>]` im Baumformat ausgeben
    ///   - `-a, --all`            alle Geräte ausgeben
    ///   - `-b, --bytes`          GRÖSSE in Bytes ausgeben, anstatt im menschenlesbaren Format
    ///   - `-d, --nodeps`         keine unter- oder übergeordneten Geräte ausgeben
    ///   - `-e, --exclude` <Liste>   Geräte nach Major-Nummer auschließen (Vorgabe: RAM-Disks)
    ///   - `-f, --fs`             Infos über Dateisysteme ausgeben
    ///   - `-i, --ascii`          nur ASCII-Zeichen verwenden
    ///   - `-l, --list`           im Listenformat ausgeben
    ///   - `-m, --perms`          Information zu Zugriffsrechten ausgeben
    ///   - `-n, --noheadings`     keine Überschriften anzeigen
    ///   - `-o, --output` <Liste> Spalten der Ausgabe
    ///   - `-p, --paths`          vollständigen Gerätepfad ausgeben
    ///   - `-r, --raw`            Rohausgabeformat verwenden
    ///   - `-s, --inverse`        Abhängigkeiten umkehren
    ///   - `-t, --topology`       Information zur Topologie ausgeben
    ///   - `-w, --width` <Breite> Breite der Ausgabe als Anzahl der Zeichen
    ///   - `-x, --sort` <Spalte>  Ausgabe nach <Spalte> sortieren
    ///   - `-y, --shell`          Spaltennamen verwenden, die als Shell-Variablen nutzbar sind
    ///   - `-z, --zoned`          Zonenbezogene Informationen ausgeben
    ///   - `--sysroot <Verz>` angegebenes Verzeichnis als Systemwurzel verwenden
    ///   - `-h, --help`           diese Hilfe anzeigen
    ///   - `-V, --version`        Version anzeigen
    public enum LsblkOption implements INativeCommandOption {

        NO_EMPTY("noempty", "A"),
        DISCARD("discard", "D"),
        DE_DUP("dedup", "E"),
        INCLUDE("include", "I"),
        JSON("json", "J"),
        MERGE("merge", "M"),
        OUTPUT_ALL("output-all", "O"),
        PAIRS("pairs", "P"),
        SCSI("scsi", "S"),
        TREE("tree", "T"),
        ALL("all", "a"),
        BYTES("bytes", "b"),
        NO_DEPS("nodeps", "d"),
        EXCLUDE("exclude", "e"),
        FS("fs", "f"),
        ASCII("ascii", "i"),
        LIST("list", "l"),
        PERMS("perms", "m"),
        NO_HEADINGS("noheadings", "n"),
        OUTPUT("output", "o"),
        PATHS("paths", "p");

        private final String fn;
        private final String sn;


        /// Lsblk Option Enum.
        ///
        /// @param fullName  full option name
        /// @param shortName short option name
        ///
        ///
        LsblkOption(final String fullName, final String shortName) {
            this.fn = "--%s".formatted(fullName);
            this.sn = "-%s".formatted(shortName);
        }


        @Override
        public String fullArg() {
            return fn;
        }

        @Override
        public Optional<String> shortArg() {
            return Optional.ofNullable(sn);
        }

    }

    /// Parameter for the command option `--output`.
    ///
    ///    - ALIGNMENT  Ausrichtungsposition
    ///    - DISC-ALN  die Ausrichtungsposition verwerfen
    ///    - DAX  Dax-fähiges Gerät
    ///    - DISC-GRAN  die Granularität verwerfen
    ///    - DISC-MAX  die maximalen Bytes verwerfen
    ///    DISC-ZERO  Datten für Nullen verwerfen
    ///    FSAVAIL  verfügbare Dateisystemgröße
    ///    FSROOTS  Wurzeln eingehängter Dateisysteme
    ///    FSSIZE  Dateisystemgröße
    ///    FSTYPE  Dateisystemtyp
    ///    FSUSED  belegte Dateisystemgröße
    ///    FSUSE%  prozentuale Dateisystembelegung
    ///    FSVER  Dateisystemversion
    ///    GROUP  Gruppenname
    ///    HCTL  Host:Kanal:Ziel:LUN für SCSI
    ///    HOTPLUG  Wechseldatenträger oder Hotplug-Gerät (USB, PCMCIA …)
    ///    KNAME  interner Kernel-Gerätename
    ///    LABEL  Dateisystem-BEZEICHNUNG
    ///    LOG-SEC  logische Sektorgröße
    ///    MAJ:MIN  Hauptversion:Nebengerätenummer
    ///    MIN-IO  Minimale E/A-Größe
    ///    MODE  Geräteknoten-Berechtigungen
    ///    MODEL  Gerätebezeichner
    ///    NAME  Gerätename
    ///    OPT-IO  Optimale E/A-Größe
    ///    OWNER  Benutzername
    ///    PARTFLAGS  Partitionsmarkierungen
    ///    PARTLABEL  Partitions-BEZEICHNUNG
    ///    PARTTYPE  Partitionstyp-Code oder -UUID
    ///    PARTTYPENAME  Partitionstypname
    ///    PARTUUID  Partitions-UUID
    ///    PATH  Pfad zum Geräteknoten
    ///    PHY-SEC  physische Sektorgröße
    ///    PKNAME  interner Kernel-Gerätename des übergeordneten Geräts
    ///    PTTYPE  Partitionstabellentyp
    ///    PTUUID  Partitionstabellenbezeichner (üblicherweise UUID)
    ///    RA  Read-ahead-Cache des Geräts
    ///    RAND  vergrößert die Zufälligkeit
    ///    REV  Geräterevision
    ///    RM  entfernbares Gerät
    ///    RO  Nur-Lese-Gerät
    ///    ROTA  Rotationsgerät
    ///    RQ-SIZE  Größe der Warteschlange für Anforderungen
    ///    SCHED  Name des E/A-Schedulers
    ///    SERIAL  Festplatten-Seriennummer
    ///    SIZE  Größe des Geräts
    ///    START  partition start offset
    ///    STATE  Status des Geräts
    ///    SUBSYSTEMS  deduplizierte Kette von Subsystemen
    ///    MOUNTPOINT  Einhängeort des Gerätes
    ///    MOUNTPOINTS  Alle Orte, in denen Geräte eingehängt sind
    ///    TRAN  Transporttyp des Gerätes
    ///    TYPE  Gerätetyp
    ///    UUID  Dateisystem-UUID
    ///    VENDOR  Gerätehersteller
    ///    WSAME  die selben maximalen Bytes werden geschrieben
    ///    WWN  eindeutiger Speicherbezeichner
    ///    ZONED  Zonenmodell
    ///    ZONE-SZ  Zonengröße
    ///    ZONE-WGRAN  Zonen-Schreibgranularität
    ///    ZONE-APP  zone append max bytes
    ///    ZONE-NR  Anzahl der Zonen
    ///    ZONE-OMAX  maximale Anzahl an geöffneten Zonen
    ///    ZONE-AMAX  maximale Anzahl an aktiven Zonen
    public enum LsblkOutputOptionArg implements Supplier<String> {
        FS_AVAILABLE("FSAVAIL"),
        FS_SIZE("FSSIZE"),
        FS_TYPE("FSTYPE"),
        LABEL("LABEL"),
        MOUNTPOINT("MOUNTPOINT"),
        NAME("NAME"),
        SERIAL("SERIAL"),
        SIZE("SIZE"),
        TYPE("TYPE"),
        TRAN("TRAN"),
        UUID("UUID"),
        VENDOR("VENDOR");
        private final String arg;

        ///
        /// Lsblk command option `--output` arguments.
        ///
        /// @param arg Argument
        ///
        LsblkOutputOptionArg(final String arg) {
            this.arg = arg;
        }

        @Override
        public String get() {
            return arg;
        }
    }

    @Override
    public LsblkCommand.Lsblk parse(String commandStdOutputStr, String commandStdErrorStr, int exitCode, Duration duration) {
        return null;
    }

    /// Lsblk Command C.
    ///
    /// @param arguments arguments
    ///
    LsblkCommand(String... arguments) {
        super("lsblk", arguments);
    }

    /**
     * Create the {@code lsblk} command with optional arguments.
     *
     * @param args optional command option/arguments
     * @return Lsblk Command
     */
    public static LsblkCommand ofArgs(String... args) {

        String[] cmdOption;
        // use defaults
        if (null == args) {
            cmdOption = DEFAULT_CMD;
        } else {
            cmdOption = Stream.of(args).filter(Objects::nonNull).toArray(String[]::new);
        }

        return new LsblkCommand(cmdOption);
    }

    /// Record of Lsblk Output.
    ///
    /// @param diskMap  map of disks(of partitions) by name
    /// @param romList  list of roms
    /// @param exitCode command exit code
    /// @param elapsed  command duration
    /// @param error    optional command error
    ///
    public record Lsblk(Map<String, Disk> diskMap,
                        List<Rom> romList,
                        int exitCode,
                        Duration elapsed,
                        Optional<String> error) implements INativeCommandResult {

        public record Disk(String name,
                           String tranType,
                           String size,
                           String fsAvail,
                           String serial,
                           List<Partition> partList) {
        }

        public record Rom(String name,
                          String tranType,
                          String size,
                          String fsAvail,
                          String fsType,
                          String serial,
                          String mount,
                          String label,
                          String uuid) {


        }

        public record Partition(String fsType,
                                String size,
                                String fsAvail,
                                String mountpoint,
                                String uuid,
                                String label,
                                String name) {
        }
    }
}