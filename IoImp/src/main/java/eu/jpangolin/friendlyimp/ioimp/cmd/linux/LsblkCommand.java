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
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
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
 *
 * @author jTzipi
 */
public final class LsblkCommand extends AbstractNativeCommand<LsblkCommand.Lsblk> {


    // LOG
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LsblkCommand.class);
    /**
     * Regular Expression to parse the columns of the 'lsblk' command.
     * WARNING: in this multiline string we must avoid ANY whitespace character
     * and any newline character, so that the regex is useful!
     * Therefor we must add the  `\` at the end of each line
     * ATTENTION: this regex did NOT cover all possible cases of values for their columns!
     * I tested it only on a linux machine
     */
    private static final String LSBLK_REG = """
            ^TYPE="(?<type>[a-z]+?)"\\s\
            TRAN="(?<tran>[a-z]*?)"\\s\
            FSTYPE="(?<fstype>[a-zA-Z0-9_]*?)"\\s\
            FSAVAIL="(?<fsavail>[0-9]*?)"\\s\
            SIZE="(?<size>[0-9]*?)"\\s\
            MOUNTPOINT="(?<mountpoint>[^"]*?)"\\s\
            SERIAL="(?<serial>[0-9A-Z]*?)"\\s\
            NAME="(?<name>[a-zA-Z0-9_-]*?)"\\s\
            LABEL="(?<label>[a-zA-Z0-9 ]*?)"\\s\
            UUID="(?<uuid>[a-zA-Z0-9-]*?)"$\
            """;
    /// Lsblk output pattern
    private static final Pattern LSBLK_CMD_PATTERN = Pattern.compile(LSBLK_REG);

    /// Default command option which is parable with the {@linkplain #LSBLK_REG lsblk regex}.
    private static final String[] DEFAULT_CMD = {LsblkOption.ASCII.fullArg(), LsblkOption.PAIRS.fullArg(), LsblkOption.BYTES.fullArg(), LsblkOption.OUTPUT.fullArg(), Stream.of(TYPE, TRAN, FS_TYPE, FS_AVAILABLE, SIZE, MOUNTPOINT, SERIAL, NAME, LABEL, UUID).map(Supplier::get).collect(Collectors.joining(","))};

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

        NO_EMPTY("noempty", "A"), DISCARD("discard", "D"), DE_DUP("dedup", "E"), INCLUDE("include", "I"), JSON("json", "J"), MERGE("merge", "M"), OUTPUT_ALL("output-all", "O"), PAIRS("pairs", "P"), SCSI("scsi", "S"), TREE("tree", "T"), ALL("all", "a"), BYTES("bytes", "b"), NO_DEPS("nodeps", "d"), EXCLUDE("exclude", "e"), FS("fs", "f"), ASCII("ascii", "i"), LIST("list", "l"), PERMS("perms", "m"), NO_HEADINGS("noheadings", "n"), OUTPUT("output", "o"), PATHS("paths", "p");

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
    /// - `ALIGNMENT`  Ausrichtungsposition
    /// - `DISC-ALN`  die Ausrichtungsposition verwerfen
    /// - `DAX`  Dax-fähiges Gerät
    /// - DISC-GRAN  die Granularität verwerfen
    /// - DISC-MAX  die maximalen Bytes verwerfen
    /// - DISC-ZERO  Datten für Nullen verwerfen
    /// - FSAVAIL  verfügbare Dateisystemgröße
    /// - FSROOTS  Wurzeln eingehängter Dateisysteme
    /// - FSSIZE  Dateisystemgröße
    /// - FSTYPE  Dateisystemtyp
    /// - FSUSED  belegte Dateisystemgröße
    /// - FSUSE%  prozentuale Dateisystembelegung
    /// - FSVER  Dateisystemversion
    /// - GROUP  Gruppenname
    /// - HCTL  Host:Kanal:Ziel:LUN für SCSI
    /// - HOTPLUG  Wechseldatenträger oder Hotplug-Gerät (USB, PCMCIA …)
    /// - KNAME  interner Kernel-Gerätename
    /// - LABEL  Dateisystem-BEZEICHNUNG
    /// - LOG-SEC  logische Sektorgröße
    /// - MAJ:MIN  Hauptversion:Nebengerätenummer
    /// - MIN-IO  Minimale E/A-Größe
    /// - MODE  Geräteknoten-Berechtigungen
    /// - MODEL  Gerätebezeichner
    /// - NAME  Gerätename
    /// - OPT-IO  Optimale E/A-Größe
    /// - OWNER  Benutzername
    /// - PARTFLAGS  Partitionsmarkierungen
    /// - PARTLABEL  Partitions-BEZEICHNUNG
    /// - PARTTYPE  Partitionstyp-Code oder -UUID
    /// - PARTTYPENAME  Partitionstypname
    /// - PARTUUID  Partitions-UUID
    /// - PATH  Pfad zum Geräteknoten
    /// - PHY-SEC  physische Sektorgröße
    /// - PKNAME  interner Kernel-Gerätename des übergeordneten Geräts
    /// - PTTYPE  Partitionstabellentyp
    /// - PTUUID  Partitionstabellenbezeichner (üblicherweise UUID)
    /// - RA  Read-ahead-Cache des Geräts
    /// - RAND  vergrößert die Zufälligkeit
    /// - REV  Geräterevision
    /// - RM  entfernbares Gerät
    /// - RO  Nur-Lese-Gerät
    /// - ROTA  Rotationsgerät
    /// - RQ-SIZE  Größe der Warteschlange für Anforderungen
    /// - SCHED  Name des E/A-Schedulers
    /// - SERIAL  Festplatten-Seriennummer
    /// - SIZE  Größe des Geräts
    /// - START  partition start offset
    /// - STATE  Status des Geräts
    /// - SUBSYSTEMS  deduplizierte Kette von Subsystemen
    /// - MOUNTPOINT  Einhängeort des Gerätes
    /// - MOUNTPOINTS  Alle Orte, in denen Geräte eingehängt sind
    /// - TRAN  Transporttyp des Gerätes
    /// - TYPE  Gerätetyp
    /// - UUID  Dateisystem-UUID
    /// - VENDOR  Gerätehersteller
    /// - WSAME  die selben maximalen Bytes werden geschrieben
    /// - WWN  eindeutiger Speicherbezeichner
    /// - ZONED  Zonenmodell
    /// - ZONE-SZ  Zonengröße
    /// - ZONE-WGRAN  Zonen-Schreibgranularität
    /// - ZONE-APP  zone append max bytes
    /// - ZONE-NR  Anzahl der Zonen
    /// - ZONE-OMAX  maximale Anzahl an geöffneten Zonen
    /// - ZONE-AMAX  maximale Anzahl an aktiven Zonen
    public enum LsblkOutputOptionArg implements Supplier<String> {
        FS_AVAILABLE("FSAVAIL"), FS_SIZE("FSSIZE"), FS_TYPE("FSTYPE"), LABEL("LABEL"), MOUNTPOINT("MOUNTPOINT"), NAME("NAME"), SERIAL("SERIAL"), SIZE("SIZE"), TYPE("TYPE"), TRAN("TRAN"), UUID("UUID"), VENDOR("VENDOR");
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
        return parseLsblk(commandStdOutputStr, commandStdErrorStr, exitCode, duration);
    }

    /// Lsblk Command C.
    ///
    /// @param arguments arguments
    ///
    LsblkCommand(String... arguments) {
        super("lsblk", arguments);
    }

    ///
    /// Return the command with the default parameter set.
    /// The default command line is `lsblk -bPio TYPE,TRAN,FSTYPE,FSAVAIL,SIZE,MOUNTPOINT,SERIAL,NAME,LABEL,UUID`
    ///
    /// @return instance
    ///
    public static LsblkCommand ofDefault() {
        return ofArgs();
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
        if (null == args || 0 == args.length) {
            cmdOption = DEFAULT_CMD;
        } else {
            cmdOption = Stream.of(args).filter(Objects::nonNull).toArray(String[]::new);
        }

        return new LsblkCommand(cmdOption);
    }

    @Override
    public String toString() {
        return "Native Linux command '" + getBaseCommand() + "'\n" + "full command : '" + getFullCommand() + "'";
    }

    // ------------------------ parse the result of `lsblk` -------------------------------

    private static Lsblk parseLsblk(String rawResultStr, String rawErrorStr, int exitCode, Duration duration) {
        assert null != rawResultStr : "Raw result is null!";

        LOG.info("<<parseLsblk>> raw result = '{}'", rawResultStr);
        // Disk map and Rom list
        final Map<String, Lsblk.Disk> diskMap = new HashMap<>();
        final List<Lsblk.Rom> romList = new ArrayList<>();

        // disk to put partition
        Lsblk.Disk lastDisk = null;
        // partition to put (optional) Logical Volumes
        Lsblk.Partition lastPart = null;


        for (String line : rawResultStr.split("\n")) {

            // parse raw line
            EnumMap<LsblkOutputOptionArg, String> rowMap = parseLsblkRow(line);
            // switch type - must be nonnull!
            String type = rowMap.get(LsblkOutputOptionArg.TYPE);

            if (null == type) {

                LOG.error("Lsblk Type is null!");
                continue;
            }


            //
            // -- lsblk prints always the disk first
            //    ,so we MUST have a valid disk before parsing partition
            //    .ROM seem to be always one line
            switch (type) {
                case "disk" -> {

                    lastDisk = Lsblk.Disk.of(rowMap);
                    diskMap.put(lastDisk.name(), lastDisk);
                }
                case "rom" -> romList.add(Lsblk.Rom.of(rowMap));
                case "part" -> {
                    lastPart = Lsblk.Partition.of(rowMap);
                    if (null == lastDisk) {
                        throw new IllegalStateException("'Lsblk' try to add partition to Last disk which is null!");
                    }

                    diskMap.get(lastDisk.name()).partList().add(lastPart);
                }
                case "lvm" -> {
                    Lsblk.LogicalVolume lv = Lsblk.LogicalVolume.of(rowMap);
                    if (null == lastPart) {
                        throw new IllegalStateException("'Lslbk' try to add logical volumen to not existing last partition!");
                    }
                    lastPart.logicalVolumeList().add(lv);
                }
                default -> throw new IllegalStateException("'Lsblk-Type' unknown '" + type);


            }
        }

        return new Lsblk(diskMap, romList, exitCode, duration, Optional.ofNullable(rawErrorStr));
    }

    /**
     * Split the raw 'lsblk' answer row vise into parts and put them into
     * the enum map.
     * <p>
     * Format should be like
     * {@code TYPE="disk" TRAN="usb" FSTYPE="" MOUNTPOINT="" SERIAL="575844314536334D4C544A37" LABEL="" NAME="sdb" UUID=""}
     * </p>
     *
     * @param row raw parsed row
     * @return map of {@linkplain LsblkOutputOptionArg} and Value
     */
    private static EnumMap<LsblkOutputOptionArg, String> parseLsblkRow(String row) {
        assert null != row : "parsed row must non null!";
        LOG.info("<<parseLsblkRow>> Try to parse row = '{}'", row);
        Matcher matcher = LSBLK_CMD_PATTERN.matcher(row);
        boolean found = matcher.find();
        LOG.info("<<parseLsblkRow>> Found Lsblk option = {}", found);
        EnumMap<LsblkOutputOptionArg, String> ret = new EnumMap<>(LsblkOutputOptionArg.class);

        for (LsblkOutputOptionArg lsblkColumn : EnumSet.of(TYPE, FS_TYPE, FS_AVAILABLE, SIZE, MOUNTPOINT, SERIAL, NAME, LABEL, UUID)) {
            String cgrp = lsblkColumn.get().toLowerCase();
            LOG.info("<<<<parseLsblkRow>> Suche nach CGrp '{}'", cgrp);
            ret.put(lsblkColumn, matcher.group(cgrp));
        }

        return ret;

    }

    /// Record of Lsblk Output.
    /// TODO: define interfaces for the common properties of
    ///  {@linkplain Disk},
    ///  {@linkplain Partition},
    ///  {@linkplain Rom},
    ///  {@linkplain LogicalVolume}
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

        /// A physical `disk`.
        ///
        /// @param name     name of disk
        /// @param tranType transport type
        /// @param size     size of disk
        /// @param fsAvail  available size
        /// @param serial   serial
        /// @param partList partition list
        ///
        public record Disk(String name,
                           String tranType,
                           String size,
                           String fsAvail,
                           String serial,
                           List<Partition> partList) {

            /**
             * Create a new instance of type `disk` which is a physical device which may contains one ore
             * more partitions.
             *
             * @param diskOptionMap options parsed of this disk
             * @return instance
             * @throws NullPointerException if {@code diskOptionMap}
             * @apiNote The values of the queries option args are never {@code null}
             * since the command always yield the `""` (empty string) if no value is present
             * Hint: the partition list is at the point we discover the disk not available! All partitions will be
             * added later
             */
            public static Disk of(Map<LsblkOutputOptionArg, String> diskOptionMap) {
                Objects.requireNonNull(diskOptionMap);

                String name = diskOptionMap.get(NAME);
                String tranType = diskOptionMap.get(TRAN);
                String size = diskOptionMap.get(SIZE);
                String fileSysAvail = diskOptionMap.get(FS_AVAILABLE);
                String serial = diskOptionMap.get(SERIAL);

                return new Disk(name, tranType, size, fileSysAvail, serial, new ArrayList<>());
            }

        }

        /// A `rom` disk.
        ///
        /// @param name       name
        /// @param tranType   transport type
        /// @param size       size of rom
        /// @param fsAvail    available bytes
        /// @param fsType     file system type
        /// @param serial     serial number
        /// @param mountpoint mountpoint point
        /// @param label      label
        /// @param uuid       universal unique id
        ///
        public record Rom(String name,
                          String tranType,
                          String size,
                          String fsAvail,
                          String fsType,
                          String serial,
                          String mountpoint,
                          String label,
                          String uuid) {

            /// Create a new `rom` instance.
            ///
            /// @param romOptionMap command option argument map
            /// @return new instance
            /// @throws NullPointerException if {@code romOptionMap}
            public static Rom of(final Map<LsblkOutputOptionArg, String> romOptionMap) {
                Objects.requireNonNull(romOptionMap);

                String name = romOptionMap.get(NAME);
                String tranType = romOptionMap.get(TRAN);
                String size = romOptionMap.get(SIZE);
                String fileSysAvail = romOptionMap.get(FS_AVAILABLE);
                String fileSysType = romOptionMap.get(FS_TYPE);
                String serial = romOptionMap.get(SERIAL);
                String mount = romOptionMap.get(MOUNTPOINT);
                String label = romOptionMap.get(LABEL);
                String uuid = romOptionMap.get(UUID);

                return new Rom(name, tranType, size, fileSysAvail, fileSysType, serial, mount, label, uuid);
            }
        }

        /// A `partition` of a previously found `disk`.
        ///
        /// @param fsType            file system type
        /// @param size              size of partition
        /// @param fsAvail           available bytes
        /// @param mountpoint        mountpoint point
        /// @param uuid              universal unique id
        /// @param label             label of partition
        /// @param name              name of partition
        /// @param logicalVolumeList list of (optional) logical volumes
        ///
        public record Partition(String fsType,
                                String size,
                                String fsAvail,
                                String mountpoint,
                                String uuid,
                                String label,
                                String name,
                                List<LogicalVolume> logicalVolumeList) {

            /**
             * Create a new `partition` of previously found `disk`.
             *
             * @param partitionOptionMap option map for the partition
             * @return new partition
             * @throws NullPointerException if {@code partitionOptionMap}
             */
            public static Partition of(final Map<LsblkOutputOptionArg, String> partitionOptionMap) {
                Objects.requireNonNull(partitionOptionMap);

                Lsblk.Partition part = new Lsblk.Partition(partitionOptionMap.get(FS_TYPE),
                        partitionOptionMap.get(SIZE),
                        partitionOptionMap.get(FS_AVAILABLE),
                        partitionOptionMap.get(MOUNTPOINT),
                        partitionOptionMap.get(UUID),
                        partitionOptionMap.get(LABEL),
                        partitionOptionMap.get(NAME),
                        new ArrayList<>());

                LOG.info("<<of>> parse Partition = '{}'", part);
                return part;
            }
        }

        /// A `logical volume` of a `logical volume group`.
        ///
        /// @param fsType     file system type
        /// @param size       size of partition
        /// @param mountpoint mountpoint point
        /// @param serial     serial
        /// @param name       name of partition
        /// @param label      label of partition
        /// @param uuid       universal unique id
        ///
        ///
        public record LogicalVolume(String fsType,
                                    String size,
                                    String mountpoint,
                                    String serial,
                                    String name,
                                    String label,
                                    String uuid) {
            /// Create a new `logical volume` of a `partition`.
            ///
            /// @param lvOptionMap map of option args/values
            /// @return new instance
            /// @throws NullPointerException if {@code lvOptionMap}
            ///
            public static LogicalVolume of(final Map<LsblkOutputOptionArg, String> lvOptionMap) {
                Objects.requireNonNull(lvOptionMap);


                String fsType = lvOptionMap.get(FS_TYPE);
                String size = lvOptionMap.get(SIZE);
                String mountpoint = lvOptionMap.get(MOUNTPOINT);
                String serial = lvOptionMap.get(SERIAL);
                String name = lvOptionMap.get(NAME);
                String label = lvOptionMap.get(LABEL);
                String uuid = lvOptionMap.get(UUID);
                LogicalVolume lv = new LogicalVolume(fsType, size, mountpoint, serial, name, label, uuid);

                LOG.info("<<of>> parse Logical Volume = '{}'", lv);

                return lv;
            }
        }
    }
}